/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Sound;

import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author alleew
 */
public abstract class Data {
    
    protected boolean isLooping = false;
    protected boolean isPlaying = false;
    protected float volume = 1;
    protected float pan = 0;
    protected float fade = 0;
    protected float pitch = 1;
    protected double rotation = 0;
    
    protected boolean dopplerEnabled = true;
    protected float velocityX = 0;
    protected float velocityY = 0;
    protected float velocityZ = 0;
    
    protected float x = 0;
    protected float y = 0;
    protected float z = 1;
    
    public abstract void Play();
    
    public abstract void SetLooping(boolean looping);
    
    public abstract void Stop();
    
    public abstract void Dispose();
    
    public abstract void Pause();
    
    public abstract void Resume();
    
    // STILL NEED TO REVIEW THIS FOR STREAMING DATA!
    public abstract void SetPitch(float pitch);
    
    public abstract void SetVolume(float volume);
    
    public abstract void SetHorizontalPosition(float position);
    
    public abstract void SetFade(float position);
    
    public abstract void SetX(float newX);
    
    public abstract void SetY(float newY);
    
    public abstract void SetZ(float newZ);
    
    public abstract void SetPosition(float newX, float newY, float newZ);
    
    public abstract boolean IsStreaming();
    
    public double GetX()
    {
        return x;
    }
    
    public double GetY()
    {
        return y;
    }
    
    public double GetZ()
    {
        return z;
    }
    
    public void SetVelocityX(float newX)
    {
        SetVelocity(newX, velocityY, velocityZ);
    }
    
    public void SetVelocityY(float newY)
    {
        SetVelocity(velocityX, newY, velocityZ);
    }
    
    public void SetVelocityZ(float newZ)
    {
        SetVelocity(velocityX, velocityY, newZ);
    }
    
    public abstract void SetVelocity(float x, float y, float z);
    
    public void SetRotation(double rotation)
    {
        this.rotation = rotation;
        
        double newX = Math.sin(Math.toRadians(rotation));
        double newY = 0;
        double newZ = Math.cos(Math.toRadians(rotation));
        
        SetPosition((float)newX, (float)newY, (float)newZ);
    }
    
    public void Rotate(double addRotation)
    {
        SetRotation(rotation + addRotation);
    }
    
    public boolean IsLooping()
    {
        return isLooping;
    }
    
    public float GetBalance()
    {
        return pan;
    }
    
    public float GetVolume()
    {
        return volume;
    }
    
    public float GetPitch()
    {
        return pitch;
    }
    
    public float GetFade()
    {
        return fade;
    }
    
    public double GetRotation()
    {
        return rotation;
    }
    
    public boolean IsDopplerEnabled()
    {
        return dopplerEnabled;
    }
    
    public abstract void EnableDoppler();
    
    public abstract void DisableDoppler();
    
    public abstract boolean IsPlaying();
    
    // Continues streaming data for streaming audio classes.
    // This will throw an error in non-streaming audio classes.
    public abstract void Update();
    
    protected static FileInputStream FileToStream(File file)
    {
        FileInputStream stream;
        
        try
        {
            stream = new FileInputStream(file);
        }
        catch (Throwable ex)
        {
            throw new RuntimeException("Could not load sound file with path: " + file.getAbsolutePath());
        }
        
        return stream;
    }
}