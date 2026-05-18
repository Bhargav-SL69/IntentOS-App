import pytest
from services.safety.gatekeeper import ExecutionGatekeeper
from api.internal_schemas import IntentCandidate
from api.schemas import DeviceContextSchema, SessionMemorySchema

def test_high_risk_requires_confirmation():
    gatekeeper = ExecutionGatekeeper()
    
    # "delete" is a HIGH risk keyword
    candidate = IntentCandidate(
        action_type="Click", target_node="delete", base_confidence=0.9, matched_rule="CLICK", fused_confidence=0.99
    )
    
    context = DeviceContextSchema(foregroundAppPackage="com.test", timestamp=123)
    session = SessionMemorySchema(activeWorkflowId="test", isSensitiveContext=False)
    
    # Even at 99% fused confidence, HIGH risk must be Confirm gated
    response = gatekeeper.evaluate(candidate, context, session, 1.0)
    
    assert response.actionType == "Confirm"
    assert "Are you sure" in response.prompt_message

def test_low_risk_execution():
    gatekeeper = ExecutionGatekeeper()
    
    # "scroll" is LOW risk
    candidate = IntentCandidate(
        action_type="Scroll", target_node="DOWN", base_confidence=0.8, matched_rule="SCROLL_DOWN", fused_confidence=0.85
    )
    
    context = DeviceContextSchema(foregroundAppPackage="com.test", timestamp=123)
    session = SessionMemorySchema(activeWorkflowId="test", isSensitiveContext=False)
    
    # 0.85 > 0.65 threshold for Low Risk
    response = gatekeeper.evaluate(candidate, context, session, 1.0)
    
    assert response.actionType == "Scroll"
    assert response.prompt_message is None

def test_stale_context_rejection():
    gatekeeper = ExecutionGatekeeper()
    
    # Medium risk intent with good confidence
    candidate = IntentCandidate(
        action_type="OpenApp", target_app="whatsapp", base_confidence=0.8, matched_rule="OPEN_APP", fused_confidence=0.85
    )
    
    # Missing foreground app -> 0.5 context score penalty
    context = DeviceContextSchema(foregroundAppPackage=None, timestamp=123)
    session = SessionMemorySchema(activeWorkflowId="test", isSensitiveContext=False)
    
    response = gatekeeper.evaluate(candidate, context, session, 1.0)
    
    # 0.85 * 0.5 = 0.425. 0.425 < 0.80 (Medium Threshold)
    assert response.actionType == "Clarify"
    assert response.safety_score == 0.425
    assert "Could you rephrase" in response.prompt_message
