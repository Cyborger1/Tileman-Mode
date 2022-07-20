/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, ConorLeckey <https://github.com/ConorLeckey>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tileman;

import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.Collection;
import net.runelite.client.util.ImageUtil;

public class TilemanModeOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;

	private static final BufferedImage IMAGE = ImageUtil.loadImageResource(TilemanModePlugin.class, "/lava32.png");

	private final Client client;
	private final TilemanModePlugin plugin;

	@Inject
	private TilemanModeConfig config;

	@Inject
	private TilemanModeOverlay(Client client, TilemanModeConfig config, TilemanModePlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Collection<WorldPoint> points = plugin.getPoints();
		Area area = new Area();
		for (final WorldPoint point : points)
		{
			if (point.getPlane() != client.getPlane())
			{
				continue;
			}

			//Polygon poly = getTilePoly(point);
			//if (poly != null)
			//{
			//	area.add(new Area(poly));
			//}

			//drawTile(graphics, point);
		}


		//graphics.setClip(area);
		//Canvas clientCanvas = client.getCanvas();
		//graphics.setPaint(new TexturePaint(ImageUtil.alphaOffset(IMAGE, -100), new Rectangle(32, 32)));
		//graphics.fillRect(clientCanvas.getX(), clientCanvas.getY(), clientCanvas.getWidth(), clientCanvas.getHeight());
		//graphics.drawImage(IMAGE, 0, 0, null);
		//graphics.setClip(null);

		return null;
	}

	private void drawTile(Graphics2D graphics, WorldPoint point)
	{
		Polygon poly = getTilePoly(point);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, getTileColor());
		}
	}

	private Polygon getTilePoly(WorldPoint point)
	{
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
		{
			return null;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, point);
		if (lp == null)
		{
			return null;
		}

		return Perspective.getCanvasTilePoly(client, lp);
	}

	private Color getTileColor() {
		if(config.enableTileWarnings()) {
			if (plugin.getRemainingTiles() <= 0) {
				return Color.RED;
			} else if (plugin.getRemainingTiles() <= config.warningLimit()) {
				return new Color(255, 153, 0);
			}
		}
		return config.markerColor();
	}
}
