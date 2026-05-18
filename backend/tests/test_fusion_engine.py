import pytest
from services.fusion_engine import FusionEngine
from api.internal_schemas import IntentCandidate
from api.schemas import ScreenSemanticNodeSchema, SessionMemorySchema

def test_context_boost():
    engine = FusionEngine()
    
    # Mock Candidates
    candidate_match = IntentCandidate(
        action_type="Click", target_node="send", base_confidence=0.7, matched_rule="CLICK"
    )
    candidate_miss = IntentCandidate(
        action_type="Click", target_node="cancel", base_confidence=0.8, matched_rule="CLICK"
    )
    
    # Mock UI Tree containing "Send Message"
    ui_tree = ScreenSemanticNodeSchema(
        id="root", isClickable=False, isFocused=False, isEditable=False, boundsInScreen={"l":0,"t":0,"r":0,"b":0},
        children=[
            ScreenSemanticNodeSchema(
                id="btn_send", text="Send Message", isClickable=True, isFocused=False, isEditable=False, boundsInScreen={"l":0,"t":0,"r":0,"b":0}
            )
        ]
    )
    
    session = SessionMemorySchema(activeWorkflowId="test", isSensitiveContext=False)
    
    # Initially, "cancel" has higher base confidence (0.8 vs 0.7)
    candidates = [candidate_match, candidate_miss]
    fused = engine.fuse(candidates, ui_tree, session, voice_confidence=1.0)
    
    # After fusion, the matching candidate should heavily outweigh the missing one
    top_candidate = fused[0]
    assert top_candidate.target_node == "send"
    assert top_candidate.matched_node_id == "btn_send"
    assert top_candidate.fused_confidence > candidate_miss.fused_confidence

def test_context_penalty():
    engine = FusionEngine()
    
    candidate = IntentCandidate(
        action_type="Click", target_node="send", base_confidence=0.9, matched_rule="CLICK"
    )
    
    # Mock UI Tree WITHOUT "Send"
    ui_tree = ScreenSemanticNodeSchema(
        id="root", isClickable=False, isFocused=False, isEditable=False, boundsInScreen={"l":0,"t":0,"r":0,"b":0},
        children=[]
    )
    
    session = SessionMemorySchema(activeWorkflowId="test", isSensitiveContext=False)
    
    fused = engine.fuse([candidate], ui_tree, session, voice_confidence=1.0)
    
    # High base confidence should be crushed by lack of context evidence
    assert fused[0].fused_confidence < 0.5 
