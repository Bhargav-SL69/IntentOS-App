from api.internal_schemas import IntentCandidate

class ClarificationPromptGenerator:
    
    @staticmethod
    def generate_clarify(candidate: IntentCandidate, reason: str = "") -> str:
        if candidate.action_type == "UNKNOWN":
            return "I didn't quite catch that. Could you repeat?"
            
        action_desc = candidate.target_node or candidate.target_app or candidate.global_type or "that"
        return f"I heard something about '{action_desc}', but I'm not entirely sure. Could you rephrase?"

    @staticmethod
    def generate_confirm(candidate: IntentCandidate) -> str:
        if candidate.action_type == "Click":
            return f"Are you sure you want to tap '{candidate.target_node}'?"
        elif candidate.action_type == "OpenApp":
            return f"Do you want me to open {candidate.target_app}?"
        elif candidate.action_type == "InputText":
            return f"Should I type that in?"
        else:
            return "Are you sure you want to do that?"
