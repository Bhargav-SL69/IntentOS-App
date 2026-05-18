from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any

class ScreenSemanticNodeSchema(BaseModel):
    id: str
    text: Optional[str] = None
    contentDescription: Optional[str] = None
    isClickable: bool
    isFocused: bool
    isEditable: bool
    # boundsInScreen can be passed as a dictionary for simplicity
    boundsInScreen: Dict[str, int] 
    children: List['ScreenSemanticNodeSchema'] = []

# Resolve forward reference
ScreenSemanticNodeSchema.model_rebuild()

class DeviceContextSchema(BaseModel):
    foregroundAppPackage: Optional[str] = None
    semanticTree: Optional[ScreenSemanticNodeSchema] = None
    timestamp: int

class SessionMemorySchema(BaseModel):
    activeWorkflowId: str
    isSensitiveContext: bool

class VoicePayloadSchema(BaseModel):
    transcript: str
    confidence: float

class InferenceRequest(BaseModel):
    voice: VoicePayloadSchema
    context: DeviceContextSchema
    session: SessionMemorySchema

class IntentResponse(BaseModel):
    intent: str
    contact: Optional[str] = None
    message: Optional[str] = None
