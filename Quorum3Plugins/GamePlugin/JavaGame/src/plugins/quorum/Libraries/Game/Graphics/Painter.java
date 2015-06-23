
package plugins.quorum.Libraries.Game.Graphics;

import plugins.quorum.Libraries.Game.GameState;
import plugins.quorum.Libraries.Game.GameRuntimeError;
import plugins.quorum.Libraries.Game.libGDX.Mesh;
import plugins.quorum.Libraries.Game.libGDX.Mesh.VertexDataType;
import plugins.quorum.Libraries.Game.libGDX.VertexAttribute;
import plugins.quorum.Libraries.Game.libGDX.VertexAttributes.Usage;
import plugins.quorum.Libraries.Game.libGDX.ShaderProgram;
import plugins.quorum.Libraries.Game.libGDX.Matrix4;


/**
 *
 * @author alleew
 */
public class Painter 
{
    public java.lang.Object me_ = null;
    
    private Mesh mesh;
    
    final static int SPRITE_SIZE = 20;
    
    float[] vertices;
    int index = 0;
    float inverseTexWidth = 0;
    float inverseTexHeight = 0;
    
    // boolean drawing = false; -- In Quorum
    
    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();
    
    private boolean blendingDisabled = false;
    private int blendSourceFunction = GraphicsManager.GL_SRC_ALPHA;
    private int blendDestFunction = GraphicsManager.GL_ONE_MINUS_SRC_ALPHA;
    
    private ShaderProgram shader;
    private ShaderProgram customShader = null;
    private boolean ownsShader;
    
    float colorValue; // Initialized by constructor.
    
    int renderCalls = 0;
    int totalRenderCalls = 0;
    int maxSpritesInBatch = 0;
    
    public Painter()
    {
        // The default constructor in Java does nothing. Note that the default
        // constructor in Quorum, however, will call LoadDefaultPainter().
    }
    
    public void LoadDefaultPainter()
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        SetColor(quorumBatch.color);
        
        mesh = new Mesh(VertexDataType.VertexArray, false, 4000, 6000, new VertexAttribute(Usage.Position, 2,
			ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        
        if (projectionMatrix == null)
            throw new GameRuntimeError("null matrix!");
        if (GameState.GetDisplay() == null)
            throw new GameRuntimeError("null display!");
        
        projectionMatrix.setToOrtho2D(0, 0, GameState.GetDisplay().GetWidth(), GameState.GetDisplay().GetHeight());
        
        vertices = new float[1000 * SPRITE_SIZE];
        
        int length = 6000;
        short[] indices = new short[length];
        short j = 0;
        for (int i = 0; i < length; i += 6, j += 4)
        {
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
        }
        
        mesh.setIndices(indices);
        
        shader = CreateDefaultShader();
        ownsShader = true;
    }
    
    /** Returns a new instance of the default shader used by Painter for GL2 when no shader is specified. */
    static public ShaderProgram CreateDefaultShader () 
    {
	String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
		+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
		+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
		+ "uniform mat4 u_projTrans;\n" //
		+ "varying vec4 v_color;\n" //
		+ "varying vec2 v_texCoords;\n" //
		+ "\n" //
		+ "void main()\n" //
		+ "{\n" //
		+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
		+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
		+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
		+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
		+ "}\n";
	String fragmentShader = "#ifdef GL_ES\n" //
		+ "#define LOWP lowp\n" //
		+ "precision mediump float;\n" //
		+ "#else\n" //
		+ "#define LOWP \n" //
		+ "#endif\n" //
		+ "varying LOWP vec4 v_color;\n" //
		+ "varying vec2 v_texCoords;\n" //
		+ "uniform sampler2D u_texture;\n" //
		+ "void main()\n"//
		+ "{\n" //
		+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
		+ "}";

	ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
	if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
	return shader;
    }
    
    public void Begin()
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        if (quorumBatch.IsDrawing())
            throw new GameRuntimeError("This batch is already drawing! Call End() before calling Begin() again.");
        
        renderCalls = 0;
        GameState.nativeGraphics.glDepthMask(false);
        if (customShader != null)
            customShader.begin();
        else
            shader.begin();
        SetupMatrices();
        quorumBatch.drawing = true;
    }
    
    public void End()
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        if (!quorumBatch.IsDrawing())
            throw new GameRuntimeError("This batch isn't drawing yet! Call Begin() before calling End().");
        
        if (index > 0)
            Flush();
        
        quorumBatch.lastTexture = null;
        quorumBatch.drawing = false;
        
        GraphicsManager gl = GameState.nativeGraphics;
        gl.glDepthMask(true);
        if (IsBlendingEnabled())
            gl.glDisable(GraphicsManager.GL_BLEND);
        
        if (customShader != null)
            customShader.end();
        else
            shader.end();
    }
    
    public void SetColor(quorum.Libraries.Game.Graphics.Color_ newColor)
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        quorumBatch.color = newColor;
        colorValue = (float)quorumBatch.color.EncodeColorAsNumber();
    }
    
    public void SetColor(double r, double g, double b, double a)
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        quorumBatch.color.SetColor(r, g, b, a);
        colorValue = (float)quorumBatch.color.EncodeColorAsNumber();
    }
    
    public void Draw (quorum.Libraries.Game.Graphics.Drawable_ drawable) 
    {
        Draw(drawable, 0, 0, false);
    }
    
    public void Draw (quorum.Libraries.Game.Graphics.Drawable_ drawable, double globalOffsetX, double globalOffsetY, boolean forceUpdate)
    {
        
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
	if (!quorumBatch.IsDrawing())
            throw new GameRuntimeError("Painter.Begin() must be called before Draw.");

        int verticesLength = vertices.length;
        if (drawable.GetTexture() != quorumBatch.lastTexture) {
            SwitchTexture(drawable.GetTexture());
        }
        else {
            if (verticesLength - index < drawable.Get_Libraries_Game_Graphics_Drawable__DRAWABLE_SIZE_()) {
                Flush();
            }
        }
        // There will need to be casting from double to float here.
        // Note that currently, "GetVertices" sets the x and y vertices, so it
        // must be used to draw (otherwise no x and y info is stored). However,
        // conveniently accessing a Quorum array's data from Java isn't possible
        // so the actual variable isn't used. This is an inelegant solution,
        // and a better one should be used here eventually.
        drawable.PrepareVertices(/*globalOffsetX, globalOffsetY, forceUpdate*/);
        
        if (!drawable.UseCustomColor())
        {
            for (int i = 2; i < drawable.Get_Libraries_Game_Graphics_Drawable__DRAWABLE_SIZE_(); i = i + drawable.Get_Libraries_Game_Graphics_Drawable__VERTEX_SIZE_())
                drawable.SetVertex(i, colorValue);
        }
        
        for (int i = 0; i < drawable.Get_Libraries_Game_Graphics_Drawable__DRAWABLE_SIZE_(); i++)
        {
            vertices[index++] = (float)drawable.GetVertex(i);
        }
    }
    
    /*public void Draw (quorum.Libraries.Game.Graphics.Sprite_ sprite) 
    {
        
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
	if (!quorumBatch.IsDrawing())
            throw new GameRuntimeError("Painter.Begin() must be called before Draw.");

        int verticesLength = vertices.length;
        if (sprite.GetTexture() != quorumBatch.lastTexture)
            SwitchTexture(sprite.GetTexture());
        else
            if (verticesLength - index < sprite.Get_Libraries_Game_Graphics_Sprite_SPRITE_SIZE())
                Flush();

        // There will need to be casting from double to float here.
        // Note that currently, "GetVertices" sets the x and y vertices, so it
        // must be used to draw (otherwise no x and y info is stored). However,
        // conveniently accessing a Quorum array's data from Java isn't possible
        // so the actual variable isn't used. This is an inelegant solution,
        // and a better one should be used here eventually.
        sprite.PrepareVertices();
        for (int i = 0; i < sprite.Get_Libraries_Game_Graphics_Sprite_SPRITE_SIZE(); i++)
        {
            vertices[index++] = (float)sprite.GetVertex(i);
        }
    }*/
    
    public void Draw (quorum.Libraries.Game.Graphics.Drawable_ sprite, double x, double y)
    {
        sprite.SetPosition(x, y);
        Draw(sprite);
    }
    
    public void Draw(quorum.Libraries.Game.Graphics.Texture_ drawTexture, double x, double y)
    {
        Draw(drawTexture, x, y, drawTexture.GetWidth(), drawTexture.GetHeight());
    }
    
    public void Draw(quorum.Libraries.Game.Graphics.Texture_ drawTexture, double xValue, double yValue, double width, double height)
    {
        
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        if (!quorumBatch.IsDrawing())
            throw new GameRuntimeError("Painter.Begin() must be called before Draw.");
        
        float[] vertices = this.vertices;
        
        if (drawTexture != quorumBatch.lastTexture)
            SwitchTexture(drawTexture);
        else if (index == vertices.length)
            Flush();
        
        final float x = (float)xValue;
        final float y = (float)yValue;
        
	final float fx2 = x + (float)width;
	final float fy2 = y + (float)height;
	final float u = 0;
	final float v = 1;
	final float u2 = 1;
	final float v2 = 0;

	float color = this.colorValue;
	int idx = this.index;
	vertices[idx++] = x;
	vertices[idx++] = y;
	vertices[idx++] = color;
	vertices[idx++] = u;
	vertices[idx++] = v;

	vertices[idx++] = x;
	vertices[idx++] = fy2;
	vertices[idx++] = color;
	vertices[idx++] = u;
	vertices[idx++] = v2;

	vertices[idx++] = fx2;
	vertices[idx++] = fy2;
	vertices[idx++] = color;
	vertices[idx++] = u2;
	vertices[idx++] = v2;

	vertices[idx++] = fx2;
	vertices[idx++] = y;
	vertices[idx++] = color;
	vertices[idx++] = u2;
	vertices[idx++] = v;
        
	this.index = idx;  
    }
    
    public void Draw(quorum.Libraries.Game.Graphics.TextureRegion_ region, double x, double y)
    {
        Draw(region, x, y, region.GetRegionWidth(), region.GetRegionHeight());
    }
    
    public void Draw(quorum.Libraries.Game.Graphics.TextureRegion_ region, double xValue, double yValue, double width, double height)
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        if (!quorumBatch.IsDrawing())
            throw new GameRuntimeError("Painter.Begin() must be called before Draw.");

	float[] vertices = this.vertices;

	quorum.Libraries.Game.Graphics.Texture_ texture = region.GetTextureField();
	if (texture != quorumBatch.lastTexture) {
		SwitchTexture(texture);
	} else if (index == vertices.length) //
		Flush();

        final float x = (float)xValue;
        final float y = (float)yValue;
        
	final float fx2 = x + (float)width;
	final float fy2 = y + (float)height;
	final float u = (float)region.GetTopSide();
	final float v = (float)region.GetRightSide();
	final float u2 = (float)region.GetBottomSide();
	final float v2 = (float)region.GetLeftSide();

	float color = this.colorValue;
	int idx = this.index;
	vertices[idx++] = x;
	vertices[idx++] = y;
	vertices[idx++] = color;
	vertices[idx++] = u;
	vertices[idx++] = v;

	vertices[idx++] = x;
	vertices[idx++] = fy2;
	vertices[idx++] = color;
	vertices[idx++] = u;
	vertices[idx++] = v2;

	vertices[idx++] = fx2;
	vertices[idx++] = fy2;
	vertices[idx++] = color;
	vertices[idx++] = u2;
	vertices[idx++] = v2;

	vertices[idx++] = fx2;
	vertices[idx++] = y;
	vertices[idx++] = color;
	vertices[idx++] = u2;
	vertices[idx++] = v;
	this.index = idx;
    }
    
    public void Flush()
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        
        if (index == 0)
            return;
        
        renderCalls++;
        totalRenderCalls++;
        
        int spritesInBatch = index / 20;
        if (spritesInBatch > maxSpritesInBatch)
            maxSpritesInBatch = spritesInBatch;
        
        int count = spritesInBatch * 6;
        
        quorumBatch.lastTexture.Bind();
        mesh.setVertices(vertices, 0, index);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);
        
        if (blendingDisabled)
            GameState.nativeGraphics.glDisable(GraphicsManager.GL_BLEND);
        else
        {
            GameState.nativeGraphics.glEnable(GraphicsManager.GL_BLEND);
            if (blendSourceFunction != -1)
                GameState.nativeGraphics.glBlendFunc(blendSourceFunction, blendDestFunction);
        }
        
        
        if (customShader != null)
            mesh.render(customShader, GraphicsManager.GL_TRIANGLES, 0, count);
        else
            mesh.render(shader, GraphicsManager.GL_TRIANGLES, 0, count);
        
        index = 0;
        
    }
    
    public void DisableBlending()
    {
        if (blendingDisabled)
            return;
        Flush();
        blendingDisabled = true;
    }
    
    public void EnableBlending()
    {
        if (!blendingDisabled)
            return;
        Flush();
        blendingDisabled = false;
    }
    
    public void SetBlendFunction (int srcFunc, int dstFunc) 
    {
	if (blendSourceFunction == srcFunc && blendDestFunction == dstFunc)
            return;
	Flush();
	blendSourceFunction = srcFunc;
	blendDestFunction = dstFunc;
    }

    public int GetBlendSrcFunc () 
    {
	return blendSourceFunction;
    }

    public int GetBlendDstFunc () 
    {
	return blendDestFunction;
    }

    public void Dispose() 
    {
	mesh.dispose();
	if (ownsShader && shader != null) shader.dispose();
    }
        
    protected void SwitchTexture (quorum.Libraries.Game.Graphics.Texture_ texture) 
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        Flush();
        quorumBatch.lastTexture = texture;
        inverseTexWidth = 1.0f / (float)texture.GetWidth();
        inverseTexHeight = 1.0f / (float)texture.GetHeight();
    }
        
    public Matrix4 GetProjectionMatrix () 
    {
        return projectionMatrix;
    }

    public Matrix4 GetTransformMatrix () 
    {
        return transformMatrix;
    }

    public void SetProjectionMatrix (Matrix4 projection) 
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
	if (quorumBatch.IsDrawing()) 
            Flush();
	projectionMatrix.set(projection);
	if (quorumBatch.IsDrawing())
            SetupMatrices();
    }

    public void SetTransformMatrix (Matrix4 transform) 
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
	if (quorumBatch.IsDrawing()) 
                Flush();
    	transformMatrix.set(transform);
	if (quorumBatch.IsDrawing())
            SetupMatrices();
    }

    private void SetupMatrices () 
    {
	combinedMatrix.set(projectionMatrix).mul(transformMatrix);
	if (customShader != null) 
        {
            customShader.setUniformMatrix("u_projTrans", combinedMatrix);
	customShader.setUniformi("u_texture", 0);
	}
        else 
        {
            shader.setUniformMatrix("u_projTrans", combinedMatrix);
            shader.setUniformi("u_texture", 0);
	}
    }

    public void SetShader (ShaderProgram shader) 
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
	if (quorumBatch.IsDrawing()) 
        {
            Flush();
            if (customShader != null)
                customShader.end();
            else
                this.shader.end();
	}
	customShader = shader;
	if (quorumBatch.IsDrawing()) 
        {
            if (customShader != null)
		customShader.begin();
            else
		this.shader.begin();
            SetupMatrices();
	}
    }

    public boolean IsBlendingEnabled () 
    {
    	return !blendingDisabled;
    }
    
    public void TintShader(quorum.Libraries.Game.Graphics.Color_ color)
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        if (quorumBatch.IsDrawing())
            this.shader.setAttributef(ShaderProgram.COLOR_ATTRIBUTE, color);
    }
    
    public void ResetShaderTint()
    {
        final quorum.Libraries.Game.Graphics.Painter quorumBatch = (quorum.Libraries.Game.Graphics.Painter) me_;
        
        if (quorumBatch.IsDrawing())
            this.shader.setAttributef(ShaderProgram.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f);
    }
        
}
