from flask import Flask, request, send_file, jsonify
from flask_cors import CORS
from gtts import gTTS
from pydub import AudioSegment
import os
import torch
# Note: MusicGen (audiocraft) requires more RAM/GPU. Using basic structure for local execution.
from audiocraft.models import MusicGen

app = Flask(__name__)
CORS(app)

# Load Music Model Once
model = MusicGen.get_pretrained('facebook/musicgen-small')

@app.route('/process', methods=['POST'])
def process():
    data = request.json
    mode = data.get('mode') # 'voice' or 'music'
    text_or_prompt = data.get('input', '')
    pitch = float(data.get('pitch', 1.0))
    voice_type = data.get('voice_type', 'default')

    output_file = "wizard_output.wav"

    if mode == 'voice':
        # Generate Voice
        tts = gTTS(text=text_or_prompt, lang='en', tld='co.in')
        tts.save("temp.mp3")
        sound = AudioSegment.from_mp3("temp.mp3")
        
        # Apply Voice Character
        if voice_type == "deep":
            sound = sound._spawn(sound.raw_data, overrides={'frame_rate': int(sound.frame_rate * 0.7)})
        elif voice_type == "robotic":
            sound = sound.set_frame_rate(int(sound.frame_rate * 1.5)).set_frame_rate(44100)
            
        # Pitch/Page Adjustment
        new_rate = int(sound.frame_rate * pitch)
        sound = sound._spawn(sound.raw_data, overrides={'frame_rate': new_rate}).set_frame_rate(44100)
        sound.export(output_file, format="wav")

    elif mode == 'music':
        # Generate Music (Aggressive Beats)
        model.set_generation_params(duration=10)
        wav = model.generate([text_or_prompt])
        import scipy.io.wavfile
        scipy.io.wavfile.write(output_file, rate=32000, data=wav[0, 0].cpu().numpy())

    return send_file(output_file, as_attachment=True)

if __name__ == '__main__':
    app.run(port=5000, debug=True)
