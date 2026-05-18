from fastapi import APIRouter, HTTPException
from core.logger import get_logger
from api.schemas import InferenceRequest, IntentResponse
from services.intent_parser import IntentParser
from services.fusion_engine import FusionEngine
from services.safety.gatekeeper import ExecutionGatekeeper

router = APIRouter()
logger = get_logger("ApiRoutes")
intent_parser = IntentParser()
fusion_engine = FusionEngine()
gatekeeper = ExecutionGatekeeper()

@router.get("/health")
async def health_check():
    """Simple endpoint to verify Android-to-Backend connection"""
    logger.info("Health check pinged")
    return {"status": "ok", "system": "IntentOS AI Engine"}

@router.post("/api/v1/intent/infer", response_model=IntentResponse)
async def infer_intent(request: InferenceRequest):
    """
    Primary ingestion endpoint. Receives Voice, Screen Context, and Memory State.
    """
    logger.info(f"Received inference request for workflow: {request.session.activeWorkflowId}")
    
    # 1. Deterministic Intent Parsing
    transcript = request.voice.transcript
    response = intent_parser.parse(transcript)
    
    logger.info(f"Returning deterministic response: {response}")
    return response
