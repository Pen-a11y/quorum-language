/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Sound;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.openal.AL10.*;

/**
 *
 * @author alleew
 */
public abstract class AudioData extends Data {
    
    private int bufferID = -1;
    private long soundID = -1;
    
    private float duration;
    
    void SetUp(byte[] pcm, int channels, int sampleRate)
    {
        int bytes = pcm.length - (pcm.length % (channels > 1 ? 4 : 2));
	int samples = bytes / (2 * channels);
	duration = samples / (float)sampleRate;

	ByteBuffer buffer = ByteBuffer.allocateDirect(bytes);
	buffer.order(ByteOrder.nativeOrder());
	buffer.put(pcm, 0, bytes);
	buffer.flip();

	if (bufferID == -1) 
        {
            bufferID = alGenBuffers();
            alBufferData(bufferID, channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, buffer.asShortBuffer(), sampleRate);
	}
    }
    
    public void Play () 
    {   
	if (manager.noDevice) 
            return;
        
        int sourceID;
        
        if (manager.SoundIDIsActive(soundID))
            sourceID = (int)manager.soundIDToSource.get(soundID);
        else
        {
            sourceID = manager.ObtainSource(false);
        
            if (sourceID == -1) 
            {
                // Attempt to recover by stopping the least recently played sound
                manager.Retain(this, true);
                sourceID = manager.ObtainSource(false);
            }
            else
                manager.Retain(this, false);
            
            soundID = manager.GetSoundID(sourceID);
        }	
        // In case it still didn't work
	if (sourceID == -1) 
            return;
	
	alSourcei(sourceID, AL_BUFFER, bufferID);
	alSourcei(sourceID, AL_LOOPING, AL_FALSE);
	alSourcef(sourceID, AL_GAIN, volume);
	alSourcePlay(sourceID);
        SetHorizontalPosition(pan);
    }
    
    /*
    public long Loop () 
    {
	return Loop(1);
    }

    public long Loop (float volume) 
    {
        if (manager.noDevice) 
            return 0;
        
        int sourceID;
        
        if (manager.SoundIDIsActive(soundID))
            sourceID = (int)manager.soundIDToSource.get(soundID);
        else
        {
            sourceID = manager.ObtainSource(false);
            soundID = manager.GetSoundID(sourceID);
        }
        
        if (sourceID == -1) 
            return -1;
        
        alSourcei(sourceID, AL_BUFFER, bufferID);
        alSourcei(sourceID, AL_LOOPING, AL_TRUE);
        alSourcef(sourceID, AL_GAIN, volume);
        alSourcePlay(sourceID);
        return soundID;
    }*/
        
    @Override
    public void Stop () 
    {
        /* Original code. Replaced with a call using soundID, which will find
        the sound to stop via direct reference rather than linear search.
    
        if (manager.noDevice) 
            return;
        manager.StopSourcesWithBuffer(bufferID);
        */
        
        if (manager.noDevice)
            return;
        if (!manager.SoundIDIsActive(soundID))
            return;
        manager.StopSound(soundID);
    }

    @Override
    public void Dispose () 
    {
	if (manager.noDevice) 
            return;
	if (bufferID == -1) 
            return;
	manager.FreeBuffer(bufferID);
	alDeleteBuffers(bufferID);
	bufferID = -1;
	manager.Forget(this);
    }
    
    @Override
    public void Pause()
    {
        /* Original code. Replaced with a call using soundID, which will find
        the sound to pause via direct reference rather than linear search.
        
        if (manager.noDevice)
            return;
        manager.PauseSourcesWithBuffer(bufferID);
        */
        
        if (manager.noDevice)
            return;
        if (!manager.SoundIDIsActive(soundID))
            return;
        manager.PauseSound(soundID);
    }
    
    @Override
    public void Resume()
    {
        /* Original code. Replaced with a call using soundID, which will find
        the sound to resume via direct reference rather than linear search.
        
        if (manager.noDevice)
            return;
        manager.ResumeSourcesWithBuffer(bufferID);
        */
        
        if (manager.noDevice)
            return;
        if (!manager.SoundIDIsActive(soundID))
            return;
        manager.ResumeSound(soundID);
    }
    
    @Override
    public void SetPitch(float pitch)
    {
        if (manager.noDevice)
            return;
        
        this.pitch = pitch;
        
        if (!manager.SoundIDIsActive(soundID))
        {
            int sourceID = manager.ObtainSource(false);
            soundID = (long)manager.sourceToSoundID.get(sourceID);
        }
        manager.SetSoundPitch(soundID, pitch);
    }
    
    @Override
    public void SetVolume(float newVolume)
    {
        if (manager.noDevice)
            return;
        
        volume = newVolume;
        
        if (!manager.SoundIDIsActive(soundID))
        {
            int sourceID = manager.ObtainSource(false);
            soundID = (long)manager.sourceToSoundID.get(sourceID);
        }
        manager.SetSoundGain(soundID, volume);
    }
    
    @Override
    public void SetLooping(boolean looping)
    {
        if (manager.noDevice)
            return;
        
        isLooping = looping;
        
        if (!manager.SoundIDIsActive(soundID))
        {
            int sourceID = manager.ObtainSource(false);
            soundID = (long)manager.sourceToSoundID.get(sourceID);
        }
        manager.SetSoundLooping(soundID, looping);
    }
    
    @Override
    public void SetHorizontalPosition(float newPan)
    {
        if (manager.noDevice)
            return;
        
        pan = newPan;
        
        if (!manager.SoundIDIsActive(soundID))
        {
            int sourceID = manager.ObtainSource(false);
            soundID = (long)manager.sourceToSoundID.get(sourceID);
        }
        manager.SetSoundPan(soundID, pan);
    }
    
    // Returns the duration of the sound in seconds.
    public float GetDuration()
    {
        return duration;
    }
    
    @Override
    public boolean IsStreaming()
    {
        return false;
    }
    
    @Override
    public boolean IsPlaying()
    {
        if (!manager.SoundIDIsActive(soundID))
            return false;
        
        int sourceID = (int)manager.soundIDToSource.get(soundID);
        
        return (alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PLAYING);
    }
    
    @Override
    public boolean IsLooping()
    {
        return isLooping;
    }
    
    @Override
    public void Update()
    {
        throw new RuntimeException("This audio was not set for streaming when loaded. Call SetStreaming(true) before calling Load to allow streaming the audio.");
    }
    
    /*


	@Override
	public long play (float volume, float pitch, float pan) {
		long id = play();
		setPitch(id, pitch);
		setPan(id, pan, volume);
		return id;
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		long id = loop();
		setPitch(id, pitch);
		setPan(id, pan, volume);
		return id;
	}
*/
    
}