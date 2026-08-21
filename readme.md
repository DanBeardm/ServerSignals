# Server Signals

A configurable server-side scheduling and communication toolkit for Fabric servers.

## Features

- Timed announcements
- Chat, action bar, title, subtitle and boss bar delivery
- Rich text formatting
- Placeholders
- Scheduled server commands
- Custom join, first-join and leave messages
- Restart countdowns
- Maintenance mode
- Scheduled maintenance
- LuckPerms-compatible permissions
- Live config reload
- Config validation

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API

LuckPerms is optional but recommended.

## Installation

1. Install Fabric Loader for Minecraft 1.21.1.
2. Install Fabric API.
3. Place `serversignals-0.1.0.jar` in the server's `mods` folder.
4. Start the server.
5. Configure Server Signals in `config/ServerSignals/`.

## Configuration

Server Signals generates:

config/ServerSignals/
├── announcements.json
├── scheduled_commands.json
├── player_messages.json
├── restart.json
├── maintenance.json
└── seen_players.json

## Commands

### General

/serversignals
/serversignals help
/serversignals validate
/serversignals reload

### Announcements

/serversignals announcement list
/serversignals announcement test <id>

### Scheduled Commands

/serversignals command list
/serversignals command test <id>

### Player Messages

/serversignals player-message
/serversignals player-message test join <player>
/serversignals player-message test first_join <player>
/serversignals player-message test leave <player>

### Restart

/serversignals restart status
/serversignals restart start <duration>
/serversignals restart cancel
/serversignals restart now

### Maintenance

/serversignals maintenance status
/serversignals maintenance enable [reason]
/serversignals maintenance disable
/serversignals maintenance kick
/serversignals maintenance schedule <duration> [reason]
/serversignals maintenance schedule status
/serversignals maintenance schedule cancel

## Permissions

Server Signals supports LuckPerms through Fabric Permissions API.

| Permission | Description |
| --- | --- |
| `serversignals.validate` | Validate configuration files |
| `serversignals.reload` | Reload configuration |
| `serversignals.announcement.list` | List announcements |
| `serversignals.announcement.test` | Test announcements |
| `serversignals.scheduledcommand.list` | List scheduled command tasks |
| `serversignals.scheduledcommand.test` | Test scheduled commands |
| `serversignals.playermessage.test` | Test player messages |
| `serversignals.restart.status` | View restart status |
| `serversignals.restart.start` | Start restart countdowns |
| `serversignals.restart.cancel` | Cancel restart countdowns |
| `serversignals.restart.now` | Restart immediately |
| `serversignals.maintenance.status` | View maintenance status |
| `serversignals.maintenance.enable` | Enable maintenance |
| `serversignals.maintenance.disable` | Disable maintenance |
| `serversignals.maintenance.schedule` | Schedule maintenance |
| `serversignals.maintenance.cancel` | Cancel scheduled maintenance |
| `serversignals.maintenance.kick` | Kick non-bypass players |

## Duration Format

Durations support:

- `30s`
- `15m`
- `1h`
- `2d`

## Placeholders

Common placeholders include:

- `{online}`
- `{max_players}`
- `{server_name}`
- `{uptime}`

Player messages additionally support:

- `{player}`
- `{player_uuid}`
- `{first_join}`
- `{message_id}`

Restart messages support:

- `{restart_remaining}`
- `{restart_remaining_seconds}`
- `{restart_end_time}`

Maintenance messages support:

- `{reason}`
- `{maintenance_remaining}`
- `{maintenance_remaining_seconds}`
- `{maintenance_start_time}`

## License

MIT