import functools
from typing import List, Optional
from api.internal_schemas import IntentCandidate
from api.schemas import ScreenSemanticNodeSchema, SessionMemorySchema
from core.logger import get_logger

logger = get_logger("FusionEngine")

class FusionEngine:
    def __init__(self):
        # Probabilistic weights for Bayesian update
        self.CONTEXT_MATCH_WEIGHT = 1.5   # Massive boost if the target is found on screen
        self.CONTEXT_MISS_WEIGHT = 0.4    # Strong penalty if the target is NOT on screen
        self.MEMORY_CONTINUITY_WEIGHT = 1.1 # Slight boost if continuing a workflow

    def fuse(
        self, 
        candidates: List[IntentCandidate], 
        context_tree: Optional[ScreenSemanticNodeSchema], 
        session: SessionMemorySchema,
        voice_confidence: float
    ) -> List[IntentCandidate]:
        """
        Applies a Bayesian-inspired probabilistic update to the base confidence of each candidate.
        """
        logger.debug(f"Starting fusion for {len(candidates)} candidates.")

        for candidate in candidates:
            # 1. Start with the prior (Base rule confidence * Acoustic confidence)
            prior = candidate.base_confidence * voice_confidence
            candidate.fused_confidence = prior

            # 2. Context Evaluator (Evidence)
            if candidate.action_type in ["Click", "InputText"]:
                if not context_tree:
                    # If we need context but have none, penalize heavily
                    candidate.fused_confidence *= self.CONTEXT_MISS_WEIGHT
                    logger.debug(f"[{candidate.action_type}] Penalized: No context tree provided.")
                else:
                    target_phrase = candidate.target_node if candidate.action_type == "Click" else candidate.input_text
                    # Loosely, if typing, the target node isn't the input text, but for MVP we simplify.
                    # Usually input_text needs a focused field. Let's look for focused fields.
                    
                    if candidate.action_type == "InputText":
                        focused_node = self._find_focused_node(context_tree)
                        if focused_node:
                            candidate.fused_confidence *= self.CONTEXT_MATCH_WEIGHT
                            candidate.matched_node_id = focused_node.id
                            logger.debug(f"[{candidate.action_type}] Boosted: Found focused editable node.")
                        else:
                            candidate.fused_confidence *= self.CONTEXT_MISS_WEIGHT
                            logger.debug(f"[{candidate.action_type}] Penalized: No focused node found.")
                            
                    elif candidate.action_type == "Click" and target_phrase:
                        matched_node = self._fuzzy_search_tree(context_tree, target_phrase)
                        if matched_node:
                            candidate.fused_confidence *= self.CONTEXT_MATCH_WEIGHT
                            candidate.matched_node_id = matched_node.id
                            logger.debug(f"[{candidate.action_type}] Boosted: Found node matching '{target_phrase}' (ID: {matched_node.id}).")
                        else:
                            candidate.fused_confidence *= self.CONTEXT_MISS_WEIGHT
                            logger.debug(f"[{candidate.action_type}] Penalized: Target '{target_phrase}' not found on screen.")

            # 3. Memory Evaluator (Evidence)
            # Basic heuristic: if we have an active workflow that isn't brand new, boost slightly
            if session and session.activeWorkflowId:
                # In a full implementation, we'd check if the intent aligns with the specific workflow schema.
                candidate.fused_confidence *= self.MEMORY_CONTINUITY_WEIGHT

            # Ensure confidence is capped at 1.0
            candidate.fused_confidence = min(candidate.fused_confidence, 1.0)
            
            logger.info(f"Fusion Result | Type: {candidate.action_type} | Prior: {prior:.2f} -> Posterior: {candidate.fused_confidence:.2f}")

        # Sort candidates by final fused confidence
        candidates.sort(key=lambda x: x.fused_confidence, reverse=True)
        return candidates

    @functools.lru_cache(maxsize=256)
    def _fuzzy_search_node(self, text: Optional[str], desc: Optional[str], target: str) -> bool:
        target_lower = target.lower()
        text_match = text and target_lower in text.lower()
        desc_match = desc and target_lower in desc.lower()
        return bool(text_match or desc_match)

    def _fuzzy_search_tree(self, node: ScreenSemanticNodeSchema, target: str) -> Optional[ScreenSemanticNodeSchema]:
        """
        Recursively searches the UI tree for a node containing the target string.
        Uses simple substring inclusion with lru_cache for MVP fuzzy matching.
        """
        # Fast path exact ID match if target happens to be an ID
        if node.id and target == node.id:
            return node
            
        if node.isClickable and self._fuzzy_search_node(node.text, node.contentDescription, target):
            return node

        # Check children
        for child in node.children:
            result = self._fuzzy_search_tree(child, target)
            if result:
                return result
                
        return None

    def _find_focused_node(self, node: ScreenSemanticNodeSchema) -> Optional[ScreenSemanticNodeSchema]:
        """Finds the currently focused and editable node for text input."""
        if node.isFocused and node.isEditable:
            return node
            
        for child in node.children:
            result = self._find_focused_node(child)
            if result:
                return result
                
        return None
