package com.jenjinstudios.demo.client.ui;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.demo.client.DemoWorldClient;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

/**
 * @author Caleb Brinkman
 */
public class WorldPane extends GridPane
{
	private volatile int frameCount = 0;

	public WorldPane(DemoWorldClient worldClient, Dimension2D size) {
		WorldCanvas canvas = new WorldCanvas(worldClient, size.getWidth(), size.getHeight() - 120);
		add(canvas, 0, 0);
		Label highScoreLabel = new Label("High Score: ");
		add(highScoreLabel, 0, 1);

		final Duration oneFrameAmt = Duration.millis(1000 / (float) 60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt,
			  event -> {
				  canvas.drawWorld();
				  requestHighScore(worldClient, highScoreLabel);
			  });
		TimelineBuilder.create().cycleCount(Animation.INDEFINITE).keyFrames(oneFrame).build().play();
	}

	private void requestHighScore(DemoWorldClient worldClient, Label highScoreLabel) {
		frameCount++;
		if (frameCount % 600 == 0)
		{
			highScoreLabel.setText("High Score: " + worldClient.getHighScore());
			Message request = MessageRegistry.getInstance().createMessage("HighScoreRequest");
			worldClient.queueOutgoingMessage(request);
		}
	}
}
