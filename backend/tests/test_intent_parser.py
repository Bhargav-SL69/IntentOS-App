import pytest
from services.intent_parser import IntentParser

def test_normalize_text():
    parser = IntentParser()
    assert parser.normalize_text("Go Home, please!") == "go home please"

def test_global_home():
    parser = IntentParser()
    candidates = parser.generate_candidates("go home")
    assert candidates[0].action_type == "Global"
    assert candidates[0].global_type == "HOME"
    
def test_open_app():
    parser = IntentParser()
    candidates = parser.generate_candidates("open whatsapp")
    assert candidates[0].action_type == "OpenApp"
    assert candidates[0].target_app == "whatsapp"

def test_click_target():
    parser = IntentParser()
    candidates = parser.generate_candidates("tap the send button")
    assert candidates[0].action_type == "Click"
    assert candidates[0].target_node == "the send button"

def test_fallback():
    parser = IntentParser()
    candidates = parser.generate_candidates("this is gibberish")
    assert candidates[0].action_type == "UNKNOWN"
