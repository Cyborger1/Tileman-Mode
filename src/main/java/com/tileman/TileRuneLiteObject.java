/*
 * Copyright (c) 2021, Trevor <https://github.com/Trevor159>
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

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Animation;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import java.awt.Color;
import net.runelite.client.callback.ClientThread;

class TileRuneLiteObject
{
	private final RuneLiteObject runeLiteObject;
	private final Client client;
	private final ClientThread clientThread;
	private Color color;
	private Style style;

	@RequiredArgsConstructor
	public enum Style
	{
		FIRE(l -> l.client.loadModelData(12615).light(
			175, 1500,
			ModelData.DEFAULT_X, ModelData.DEFAULT_Y, ModelData.DEFAULT_Z),
			anim(3105))
		;

		private final Function<TileRuneLiteObject, Model> modelSupplier;
		private final Function<TileRuneLiteObject, Animation> animationSupplier;
	}

	private static Function<TileRuneLiteObject, Animation> anim(int id)
	{
		return b -> b.client.loadAnimation(id);
	}

	public TileRuneLiteObject(Client client, ClientThread clientThread, WorldPoint worldPoint, Color color, Style style, boolean activateOnSpawn)
	{
		this.client = client;
		this.clientThread = clientThread;
		runeLiteObject = client.createRuneLiteObject();

		this.color = color;
		this.style = style;
		update();
		runeLiteObject.setShouldLoop(true);

		LocalPoint lp = LocalPoint.fromWorld(client, worldPoint);
		if (lp != null)
		{
			runeLiteObject.setLocation(lp, client.getPlane());

			runeLiteObject.setActive(activateOnSpawn);
		}
	}

	public void setColor(Color color)
	{
		if (this.color != null && this.color.equals(color))
		{
			return;
		}

		this.color = color;
		update();
	}

	public void setStyle(Style style)
	{
		if (this.style == style)
		{
			return;
		}

		this.style = style;
		update();
	}

	private void update()
	{
		clientThread.invoke(() ->
		{
			Model model = style.modelSupplier.apply(this);
			if (model == null)
			{
				return false;
			}

			Animation anim = style.animationSupplier.apply(this);

			runeLiteObject.setAnimation(anim);
			runeLiteObject.setModel(model);
			return true;
		});
	}

	public void activate()
	{
		runeLiteObject.setActive(true);
	}

	public void deactivate()
	{
		runeLiteObject.setActive(false);
	}

}