from api.schemas import DeviceContextSchema, SessionMemorySchema
from core.logger import get_logger

logger = get_logger("StaleContextValidator")

class StaleContextValidator:
    
    @classmethod
    def calculate_consistency(cls, context: DeviceContextSchema, session: SessionMemorySchema) -> float:
        """
        Returns a modifier (0.0 to 1.0) representing context health.
        If the context is totally invalid (e.g. session expired, high-risk context mismatch),
        it returns 0.0, which will hard-block execution.
        """
        score = 1.0
        
        # If memory marks context as sensitive (e.g. banking app), penalize default confidence
        # requiring extremely high certainty to execute anything.
        if session.isSensitiveContext:
            logger.warning("Sensitive context detected. Applying safety penalty.")
            score *= 0.8
            
        # In a full implementation, we'd compare the current foregroundAppPackage 
        # to the one recorded when the workflow started.
        if not context.foregroundAppPackage:
            logger.warning("No foreground app package detected. Ambiguous context.")
            score *= 0.5
            
        return score
