/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.quorum.Libraries.Game.Graphics;


import quorum.Libraries.Game.Graphics.Color_;

import plugins.quorum.Libraries.Game.libGDX.Array;
import plugins.quorum.Libraries.Game.GameStateManager;

/**
 * The Texture class stores information regarding OpenGL textures, which is
 * needed each time the Texture is drawn. The bitmap used by this texture is
 * generated by PixelMap.
 */
public class Texture
{
    public java.lang.Object me_ = null;
  
    public int glTarget; //The target, such as GL_TEXTURE_2D
    protected int glHandle;

    private GraphicsManager gl20 = GameStateManager.nativeGraphics; 

    /*
    The color that should be used to render this texture by the Painter2D when
    using the font shader. If the color is null, then the font shader will not be
    used.
    */
    public Color_ fontColor = null;

    TextureData data;

    /*
    This array holds onto all of the textures which need to be reloaded after a
    context loss.
    */
    final static Array<Texture> RELOADABLE_TEXTURES = new Array<Texture>();

    public void Bind() 
    {
        gl20.glBindTexture(glTarget, glHandle);
    }
    
    public void Bind(int unit)
    {
        gl20.glActiveTexture(gl20.GL_TEXTURE0 + unit);
        gl20.glBindTexture(glTarget, glHandle);
    }
    
    public void BindToDefault()
    {
        gl20.glBindTexture(glTarget, 0);
    }

    public int CreateGLHandle()
    {
      return gl20.glGenTexture();
    }

    public void SetGL20Info(int target, int handle)
    {
        glTarget = target;
        glHandle = handle;
    }
    
    public int GetGLTarget()
    {
        return glTarget;
    }
    
    public int GetGLHandle()
    {
        return glHandle;
    }
  
    public void AddReloadableTexture()
    {
	RELOADABLE_TEXTURES.add(this);
    }
    
    public void RemoveReloadableTexture()
    {
        RELOADABLE_TEXTURES.removeValue(this, true);
    }
    
    public void Dispose()
    {
        if (glHandle != 0)
        {
            GameStateManager.nativeGraphics.glDeleteTexture(glHandle);
            glHandle = 0;
            RemoveReloadableTexture();
        }
    }
    
    /*
    Used to set the color to apply to this texture if it is being rendered by
    the Painter2D's font shader.
    */
    public void SetFontColor(Color_ color)
    {
        fontColor = color;
    }
    
    /*
    Used to retrieve the color that should be applied to this texture if it is
    being rendered by the Painter2D's font shader.
    */
    public Color_ GetFontColor()
    {
        return fontColor;
    }
    
    public static void ReloadTextures()
    {
        for (Texture texture : RELOADABLE_TEXTURES)
        {
            ((quorum.Libraries.Game.Graphics.Texture_)texture.me_).Reload();
        }
    }
}
