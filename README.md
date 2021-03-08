# ChatEmojis
Chat Emojis is a lightweight plugin that replaces emoticons with 'emojis'.
ChatEmojis allows you to add your own emojis in the config file and supports multiple emoticons, random emoji selection, and regular expression capturing. Chat Emojis comes with 36 default emojis including all of the emojis on Hypixel.

**Required Java Version:** Java 8+

# Commands
***Note:** Sub-Commands varies per language, though the permission node **stays the same**.*

| Command | Description | Permission |
| ------  | ------ | ------ |
| `/emoji help` | Shows list of commands | none |
| `/emoji info` | Shows list of emojis | none |
| `/emoji [list]` | Shows list of emojis | `chatemojis.list` |
| `/emoji reload` | Reloads all emojis | `chatemojis.reload` |
| `/emoji settings` | Shows list of emojis | `chatemojis.admin` |
| `/emoji version` | Shows the plugin's version | none |

# Permissions
| Permission Node | Default | Description
| ------ | ------ | ------ |
| chatemojis.list | Everyone | Allows access to use /emoji [list] |
| chatemojis.use.* | OP | Permission to use all emojis |
| chatemojis.reload | OP | Allows access to reload emojis |
| chatemojis.admin | OP | Allows access to change plugin settings |
| chatemojis.* | OP | Permission to utilize everything |

**Emoji-Specific Permission**:
Ungrouped emojis permission is as easy as `chatemojis.use.<name>`
Grouped emojis permission needs to include the path to the group (ex. `chatemojis.use.<group-path>.<name>`)
If you still don't understand how to get emoji-specific permission- as a server operator you're able to hover over the emoji (in the `/emoji` list) to view the permission node for that specific emoji.

# Soft-Dependencies
The plugins mentioned below are soft-dependencies, meaning that ChatEmojis does **NOT** require it for it to work.

**[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** is used for parsing placeholders which you may have in your emojis.
Please note that if you are using PlaceholderAPI, you must also install the PlaceholderAPI Extension corresponding to the placeholder you're trying to access, more about this can be found [here](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders).

**[EssentialsX](https://www.spigotmc.org/resources/essentialsx.9089/)** is used for parsing emoticons in direct messages.

# How to create your own Emoji
A full tutorial and detailed explanation on how to create your own emoji can be found [here](https://github.com/Mxlvin/ChatEmojis/wiki/How-to-create-your-own-emoji).

# Supported Languages
ChatEmojis currently only supports 1 language. If you're able to speak any other language fluently,
and if you're able to translate from English, please create a pull request.

All language files can be found [here](https://github.com/Mxlvin/ChatEmojis/tree/2.2.1/src/main/resources/lang).

| Language | Country | Translator(s) | Code |
| ------ | ------ | ------ | ------ |
| English | US | [RMJTromp](https://github.com/Mxlvin) | en_US |

# Emoji List (Screenshot)
![List of Emojis](https://i.imgur.com/B0s6wga.png)

# Official Resouce Links
[SpigotMC](https://www.spigotmc.org/resources/chatemojis.88027/), [MC-Market](https://www.mc-market.org/resources/19063/).

# License
This project is subject to the [GNU General Public License v3.0](https://github.com/Mxlvin/ChatEmojis/blob/main/LICENSE). This does only apply for source code located directly in this clean repository.
For those who are unfamiliar with the license, here is a summary of its main points. This is by no means legal advice nor legally binding.
You are allowed to
 - use
 - share
 - modify

this project entirely or partially for free and even commercially. However, please consider the following:

 - **You must disclose the source code of your modified work and the source code you took from this project. This means you are not allowed to use code from this project (even partially) in a closed-source (or even obfuscated) application.**
 - **Your modified application must also be licensed under the GPL**

Do the above and share your source code with everyone; just like we do.
