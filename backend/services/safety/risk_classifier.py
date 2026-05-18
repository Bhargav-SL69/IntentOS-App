from enum import Enum
from api.internal_schemas import IntentCandidate
from core.logger import get_logger

logger = get_logger("IntentRiskClassifier")

class RiskLevel(Enum):
    LOW = "LOW"       # 0.65 - 0.75 threshold
    MEDIUM = "MEDIUM" # 0.80 - 0.85 threshold
    HIGH = "HIGH"     # 0.90 - 0.95 threshold

class IntentRiskClassifier:
    
    HIGH_RISK_KEYWORDS = ["delete", "remove", "send", "pay", "buy", "transfer", "confirm", "clear"]
    
    @classmethod
    def classify(cls, candidate: IntentCandidate) -> RiskLevel:
        """
        Categorizes the risk of an intent. 
        Destructive or state-changing actions are HIGH.
        App switching or navigation is MEDIUM.
        Non-destructive UI reading/scrolling is LOW.
        """
        # 1. Check for destructive keywords in the target or input
        target_str = (candidate.target_node or "") + " " + (candidate.input_text or "")
        target_str = target_str.lower()
        
        if any(keyword in target_str for keyword in cls.HIGH_RISK_KEYWORDS):
            return RiskLevel.HIGH
            
        if candidate.action_type == "InputText":
            # Inputting text is generally high/medium risk because it modifies state
            return RiskLevel.HIGH
            
        # 2. Check Action Types
        if candidate.action_type == "OpenApp":
            return RiskLevel.MEDIUM
            
        if candidate.action_type == "Global":
            if candidate.global_type in ["HOME", "RECENTS"]:
                return RiskLevel.MEDIUM
            return RiskLevel.LOW # BACK, NOTIFICATIONS
            
        if candidate.action_type == "Scroll":
            return RiskLevel.LOW
            
        # Default fallback for Click
        return RiskLevel.MEDIUM
