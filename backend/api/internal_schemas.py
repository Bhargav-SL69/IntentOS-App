from pydantic import BaseModel
from typing import Optional

class IntentCandidate(BaseModel):
    action_type: str # "Click", "Scroll", "Global", "OpenApp", "InputText"
    target_node: Optional[str] = None
    target_app: Optional[str] = None
    global_type: Optional[str] = None
    input_text: Optional[str] = None
    base_confidence: float
    matched_rule: str
    
    # Fusion properties
    fused_confidence: float = 0.0
    matched_node_id: Optional[str] = None
