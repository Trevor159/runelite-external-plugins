package gg.trevor.tobdamage;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "TOB Damage Counter",
	description = "Shows personal and total damage for each room in the theatre of blood",
	tags = {"counter", "tracker"}
)
public class TobDamageCounterPlugin extends Plugin
{
	private static final Set<Integer> blacklistNPCs = ImmutableSet.of(NpcID.SUPPORTING_PILLAR);
	private static final Set<Integer> SALVE_IDS = ImmutableSet.of(ItemID.SALVE_AMULET_E, ItemID.SALVE_AMULETEI, ItemID.SALVE_AMULET, ItemID.SALVE_AMULETI);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");
	private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#,###");

	// world point they put a player in while they check if he is in a raid
	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3370, 5152, 2);

	private static final int VERZIK_HEAL_GRAPHIC = 1602;

	private static final Set<Integer> maidenSpawns = ImmutableSet.of(NpcID.NYLOCAS_MATOMENOS, NpcID.BLOOD_SPAWN);
	private static final Set<Integer> verzikIDs = ImmutableSet.of(NpcID.VERZIK_VITUR_8370, NpcID.VERZIK_VITUR_8372, NpcID.VERZIK_VITUR_8374);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TobDamageOverlay tobDamageOverlay;

	@Inject
	private TobDamageCounterConfig config;

	@Getter
	private boolean inTob;

	@Getter
	private TobRooms currentRoom;

	@Getter
	private Map<TobRooms, Damage> damageMap = new HashMap<>();

	@Getter
	private Damage raidDamage;

	private boolean shouldCalc;
	private boolean loggedIn;

	@Data
	class Damage
	{
		private int personalDamage = 0;
		private int totalDamage = 0;
		private int totalHealing = 0;
		private Map<Player, Integer> leechCounts = new HashMap<>();

		void addDamage(int damage, boolean isLocalPlayer)
		{
			if (this != raidDamage)
			{
				if (raidDamage == null)
				{
					raidDamage = new Damage();
				}
				raidDamage.addDamage(damage, isLocalPlayer);
			}

			totalDamage += damage;


			if (isLocalPlayer)
			{
				personalDamage += damage;
			}
		}

		void addLeech(Player player)
		{
			if (leechCounts.get(player) != null)
			{
				leechCounts.put(player, leechCounts.get(player) + 1);
			}
			else
			{
				leechCounts.put(player, 1);
			}
		}

		void addHealing(int amount)
		{
			totalHealing += amount;
		}
	}

	@Provides
	TobDamageCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobDamageCounterConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(() -> calcInTob());
		overlayManager.add(tobDamageOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(tobDamageOverlay);
		reset();
	}

	private void reset()
	{
		damageMap.clear();
		inTob = false;
		currentRoom = null;
		raidDamage = null;
		shouldCalc = false;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (!inTob)
		{
			return;
		}

		Actor actor = hitsplatApplied.getActor();

		if (!(actor instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) actor;

		if (blacklistNPCs.contains(npc.getId()))
		{
			return;
		}

		calcCurrentRoom(npc.getId());

		Hitsplat hitsplat = hitsplatApplied.getHitsplat();

		if (currentRoom == null)
		{
			return;
		}

		if (config.showMVPDamage())
		{
			if (currentRoom == TobRooms.MAIDEN && maidenSpawns.contains(npc.getId()))
			{
				return;
			}

			if ((currentRoom == TobRooms.VERZIK_P1 || currentRoom == TobRooms.VERZIK_P2 || currentRoom == TobRooms.VERZIK_P3) && !verzikIDs.contains(npc.getId()))
			{
				return;
			}
		}

		if (hitsplat.isMine())
		{
			damageMap.get(currentRoom).addDamage(hitsplat.getAmount(), true);
		}
		else if (hitsplat.isOthers())
		{
			damageMap.get(currentRoom).addDamage(hitsplat.getAmount(), false);
		}
		else if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.HEAL)
		{
			damageMap.get(currentRoom).addHealing(hitsplat.getAmount());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().getId() != NpcID.VERZIK_VITUR_8375)
		{
			return;
		}

		if (config.showDamageSummary())
		{
			// raid over
			for (TobRooms room : TobRooms.values())
			{
				printRoomDamage(room, damageMap.get(room));
			}

			printRoomDamage(null, raidDamage);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && !loggedIn)
		{
			if (client.getLocalPlayer() != null && client.getLocalPlayer().getWorldLocation().equals(TEMP_LOCATION))
			{
				return;
			}

			shouldCalc = true;
		}
		else if (client.getGameState() == GameState.LOGIN_SCREEN
			|| client.getGameState() == GameState.CONNECTION_LOST)
		{
			loggedIn = false;
		}
		else if (client.getGameState() == GameState.HOPPING)
		{
			reset();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (shouldCalc)
		{
			calcInTob();
			shouldCalc = false;
			loggedIn = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		boolean tempInTob = getTobState();

		if (tempInTob != inTob)
		{
			if (loggedIn)
			{
				if (tempInTob)
				{
					initializeTob();
				}
				else
				{
					reset();
				}
			}

			inTob = tempInTob;
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (!inTob)
		{
			return;
		}

		int id = event.getActor().getGraphic();

		if (id == VERZIK_HEAL_GRAPHIC)
		{
			if (config.showLeechMessages())
			{
				String chatMessage = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append(event.getActor().getName() + " has leeched and healed Verzik.")
					.build();

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.FRIENDSCHATNOTIFICATION)
					.runeLiteFormattedMessage(chatMessage)
					.build());
			}

			if (event.getActor() instanceof Player)
			{
				damageMap.get(currentRoom).addLeech((Player) event.getActor());
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!inTob || !(event.getActor() instanceof Player))
		{
			return;
		}

		Player p = (Player) event.getActor();

		Actor interacting = p.getInteracting();
		if (interacting == null
			|| !(interacting instanceof NPC)
			|| ((NPC) interacting).getId() != NpcID.PESTILENT_BLOAT
			|| p.getAnimation() == -1)
		{
			return;
		}

		int amulet_id = p.getPlayerComposition().getEquipmentId(KitType.AMULET);

		if (SALVE_IDS.contains(amulet_id))
		{
			return;
		}

		if (config.showLeechMessages())
		{
			Integer leechCount = damageMap.get(currentRoom).getLeechCounts().get(p);
			if (leechCount == null)
			{
				String chatMessage = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append(p.getName() + " is leeching and is not attacking with a salve.")
					.build();

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.FRIENDSCHATNOTIFICATION)
					.runeLiteFormattedMessage(chatMessage)
					.build());
			}
		}

		if (event.getActor() instanceof Player)
		{
			damageMap.get(currentRoom).addLeech((Player) event.getActor());
		}
	}

	private void calcCurrentRoom(int npcID)
	{
		if (currentRoom != null && currentRoom.getNpcIds().contains(npcID))
		{
			return;
		}
		else
		{
			for (TobRooms room : TobRooms.values())
			{
				if (room.getNpcIds().contains(npcID))
				{
					currentRoom = room;
					return;
				}
			}
		}

		currentRoom = null;
		log.warn("NPC ID not handled: " + npcID);
	}

	private void calcInTob()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		boolean tempInTob = getTobState();

		if (tempInTob != inTob)
		{
			if (!tempInTob)
			{
				reset();
			}
			else if (tempInTob)
			{
				initializeTob();
			}

			inTob = tempInTob;
		}
	}

	private void initializeTob()
	{
		for (TobRooms room : TobRooms.values())
		{
			damageMap.put(room, new Damage());
		}
	}

	private boolean getTobState()
	{
		return client.getVar(Varbits.THEATRE_OF_BLOOD) == 2 || client.getVar(Varbits.THEATRE_OF_BLOOD) == 3;
	}

	private void printRoomDamage(TobRooms room, Damage damage)
	{
		int totalDamage = damage.getTotalDamage();
		int personalDamage = damage.getPersonalDamage();

		double percentage = personalDamage / (totalDamage / 100.0);

		String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Total " + (room != null ? room.toString() : "raid") + " damage: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(DAMAGE_FORMAT.format(totalDamage))
			.append(ChatColorType.NORMAL)
			.append(", Personal damage: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(DAMAGE_FORMAT.format(personalDamage))
			.append(ChatColorType.NORMAL)
			.append(" (")
			.append(ChatColorType.HIGHLIGHT)
			.append(DECIMAL_FORMAT.format(percentage))
			.append(ChatColorType.NORMAL)
			.append("%)")
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.FRIENDSCHATNOTIFICATION)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}
}
