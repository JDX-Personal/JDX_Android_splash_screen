package com.example.jdxtrial2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.Toast;

import com.example.jdxtrial2.ErrorHandler;
import com.example.jdxtrial2.JDXTrial2Renderer;
import com.example.jdxtrial2.ErrorHandler.ErrorType;

public class JDXTrial2SurfaceView extends GLSurfaceView implements ErrorHandler {
	
	private JDXTrial2Renderer renderer;
	private float density;
	
	
	public JDXTrial2SurfaceView(Context context) {
		super(context);
	}
	

	@Override
	public void handleError(final ErrorType errorType, final String cause) {
		// Queue on UI thread.
		post(new Runnable() {
			@Override
			public void run() {
				final String text;

				switch (errorType) {
				case BUFFER_CREATION_ERROR:
					text = "JDX Trial 2 Buffer Creation Failed";
					break;
				default:
					text = "JDX Trial 2 Unknown Error";
				}

				Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();

			}
		});
	}
	
	// Hides superclass method.
	public void setRenderer(JDXTrial2Renderer renderer, float density) 
	{
		this.renderer = renderer;
		this.density = density;
		super.setRenderer(renderer);
	}

}
