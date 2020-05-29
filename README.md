# Blackboard Discord Announcer
A Blackboard bot for fetching and displaying Blackboard announcements to your Discord server.
We currently support:
 * NTNU (Using an [older blackboard version](https://innsida.ntnu.no/c/wiki/get_page_attachment?p_l_id=22780&nodeId=24647&title=Blackboard+-+Bruk+av+sandkasse&fileName=1_1_finne_frem.jpg) from 2017).
 
 **NOTE:** Does the bot work with your schools Blackboard? Let us know!

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for 
development and testing purposes. We also have a Discord server to chat regarding development, 
help setting up the bot or general discussion, see this [link](https://discord.gg/a2sZxHR).

You can check out and explore the Blackboard Discord bot [here](https://discord.gg/a2sZxHR).

### Prerequisites
You need Java 11 and Maven to be able to contribute to the project.

### Setup Discord Bot

You can use and test the bot on the Discord server, but if you want to create your own 
Discord bot you can follow this [guide](https://discordpy.readthedocs.io/en/latest/discord.html). You 
need the Discord bot token as well as the Discord channel ID for the next section.

### Application Configuration
For the Blackboard bot to work, you need to copy the `application.properties.template` to 
`application.properties`. The file holds the required values for the bot to work.
A snippet template of the file is shown below. This file should **NEVER be pushed to the repository 
or shared** with anyone as it holds private information. Only have this locally.

```yaml
### NTNU BlackBoard credentials ###
username={BLACKBOARD_AUTH_USERNAME}
password={BLACKBOARD_AUTH_PASSWORD}

# How many minutes to wait to check for new announcements
interval=60

### Bot ###
#https://discordapp.com/channels/{server_id}/{channel_id} (from discord web app)
channel_id={CHANNEL_ID_LOCATED_IN_URL_OF_SELECTED_DISCORD_SERVER}

# Token for your bot (from Discord Developer Portal)
token={DISCORD_BOT_TOKEN}
```

## Built With
* [Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) - Language and 
version used
* [Maven](https://maven.apache.org/) - Dependency Management
* [JDA](https://github.com/DV8FromTheWorld/JDA) - Java Discord API

## Contributing
Contribution guide will come shortly.

## Authors
* **[PersonligFrelser](https://github.com/PersonligFrelser)** - *Initial work/creator*
* **[Anicholasson](https://github.com/ANicholasson)** - *Contributor*

See also the list of 
[contributors](https://github.com/PersonligFrelser/bb-discord-announcer/contributors) who 
participated in this project.

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
