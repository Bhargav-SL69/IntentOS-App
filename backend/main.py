from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api.routes import router as api_router
from core.logger import get_logger

logger = get_logger("IntentOS_Main")

app = FastAPI(
    title="IntentOS AI Backend",
    description="Privacy-preserving local AI engine for IntentOS smartphone copilot.",
    version="1.0.0"
)

# Configure CORS for local emulator/device communication
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Since it's a local backend, allow all local origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)

@app.on_event("startup")
async def startup_event():
    logger.info("IntentOS AI Engine is starting up...")

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("IntentOS AI Engine is shutting down...")
