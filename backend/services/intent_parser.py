import re
import string
from core.logger import get_logger
from api.schemas import IntentResponse

logger = get_logger("IntentParser")

class IntentParser:
    def __init__(self):
        self.contacts = {
            "mom": "+91XXXXXXXXXX",
            "john": "+91XXXXXXXXXX"
        }
        
    def normalize_text(self, text: str) -> str:
        text = text.lower().strip()
        text = text.translate(str.maketrans('', '', string.punctuation))
        return text

    def parse(self, transcript: str) -> IntentResponse:
        text = self.normalize_text(transcript)
        logger.debug(f"Parsing normalized transcript: '{text}'")

        # 1. Send Message
        # Matches: "send whatsapp message to mom saying I will be late" or "message john on whatsapp hello"
        send_match = re.search(r'(?:send whatsapp message to|message)\s+(?P<contact>\w+)(?:\s+on whatsapp)?\s+(?:saying\s+)?(?P<message>.+)', text)
        if send_match:
            contact_name = send_match.group("contact")
            number = self.contacts.get(contact_name, contact_name)
            return IntentResponse(
                intent="SEND_WHATSAPP_MESSAGE",
                contact=number,
                message=send_match.group("message").strip()
            )

        # 2. Call
        # Matches: "call mom on whatsapp" or "make whatsapp call to john"
        call_match = re.search(r'(?:call|make whatsapp call to)\s+(?P<contact>\w+)(?:\s+on whatsapp)?', text)
        if call_match:
            contact_name = call_match.group("contact")
            number = self.contacts.get(contact_name, contact_name)
            return IntentResponse(
                intent="WHATSAPP_CALL",
                contact=number
            )

        # 3. Open
        # Matches: "open whatsapp" or "launch whatsapp"
        open_match = re.search(r'(?:open|launch)\s+whatsapp', text)
        if open_match:
            return IntentResponse(intent="OPEN_WHATSAPP")

        # Fallback to OPEN if nothing else matches (for robust demo)
        return IntentResponse(intent="OPEN_WHATSAPP")
