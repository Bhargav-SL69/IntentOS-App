from api.internal_schemas import IntentCandidate
from api.schemas import IntentResponse, DeviceContextSchema, SessionMemorySchema
from services.safety.risk_classifier import IntentRiskClassifier, RiskLevel
from services.safety.context_validator import StaleContextValidator
from services.safety.prompt_generator import ClarificationPromptGenerator
from core.logger import get_logger

logger = get_logger("ExecutionGatekeeper")

class ExecutionGatekeeper:
    
    def __init__(self):
        # Strict Execution Thresholds
        self.THRESHOLDS = {
            RiskLevel.HIGH: 0.90,
            RiskLevel.MEDIUM: 0.80,
            RiskLevel.LOW: 0.65
        }

    def evaluate(self, candidate: IntentCandidate, context: DeviceContextSchema, session: SessionMemorySchema, voice_conf: float) -> IntentResponse:
        
        risk_level = IntentRiskClassifier.classify(candidate)
        context_score = StaleContextValidator.calculate_consistency(context, session)
        
        # Composite Safety Score Formula
        # Fused confidence already incorporates Context Node Matches.
        # We now multiply by the Stale Context modifier.
        safety_score = candidate.fused_confidence * context_score
        
        threshold = self.THRESHOLDS[risk_level]
        
        logger.info(f"Gatekeeper | Action: {candidate.action_type} | Risk: {risk_level.value} | SafetyScore: {safety_score:.2f} | Req: {threshold}")

        # 1. Total Rejection (Below Low Threshold or completely unknown)
        if safety_score < self.THRESHOLDS[RiskLevel.LOW] or candidate.action_type == "UNKNOWN":
            return IntentResponse(
                actionType="Clarify",
                confidence=safety_score,
                safety_score=safety_score,
                prompt_message=ClarificationPromptGenerator.generate_clarify(candidate),
                reasoning=f"Rejected: SafetyScore {safety_score:.2f} < Minimum Threshold"
            )
            
        # 2. Soft Rejection (Falls below its required tier threshold)
        if safety_score < threshold:
            return IntentResponse(
                actionType="Clarify",
                confidence=safety_score,
                safety_score=safety_score,
                prompt_message=ClarificationPromptGenerator.generate_clarify(candidate),
                reasoning=f"Clarify: SafetyScore {safety_score:.2f} < {risk_level.value} Threshold {threshold}"
            )
            
        # 3. High Risk Gating (Even if above threshold, force confirmation for destructive actions)
        if risk_level == RiskLevel.HIGH:
            # We never blindly execute HIGH risk for MVP. We ALWAYS ask for confirmation first.
            return IntentResponse(
                actionType="Confirm",
                confidence=safety_score,
                safety_score=safety_score,
                prompt_message=ClarificationPromptGenerator.generate_confirm(candidate),
                targetNodeId=candidate.matched_node_id or candidate.target_node, # Pass targets so the Android UI can highlight them
                reasoning=f"Confirm: High-Risk Action gated. SafetyScore {safety_score:.2f} >= {threshold}"
            )
            
        # 4. Execution Allowed
        return IntentResponse(
            actionType=candidate.action_type,
            targetNodeId=candidate.matched_node_id or candidate.target_node,
            targetPackageName=candidate.target_app,
            globalActionType=candidate.global_type,
            inputText=candidate.input_text,
            confidence=candidate.fused_confidence,
            safety_score=safety_score,
            reasoning=f"Allowed: SafetyScore {safety_score:.2f} >= {risk_level.value} Threshold {threshold}"
        )
