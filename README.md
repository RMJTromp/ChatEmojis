# ChatEmojis
Chat Emojis is a lightweight plugin that replaces emoticons with 'emojis'.
ChatEmojis allows you to add your own emojis in the config file and supports multiple emoticons, random emoji selection, and regular expression capturing. Chat Emojis comes with 36 default emojis including all of the emojis on Hypixel.

**Required Java Version:** Java 8+

# Commands
| Command | Description | Permission |
| ------  | ------ | ------ |
| `/emoji [list]` | Shows list of emojis | `chatemojis.list` |
| `/emoji help` | Shows list of commands | none |
| `/emoji reload` | Reloads all emojis | `chatemojis.reload` |
| `/emoji version` | Shows the plugin's version | none |
| `/emoji settings` | Shows list of emojis | `chatemojis.admin` |

# Permissions
| Permission Node | Default | Description
| ------ | ------ | ------ |
| chatemojis.list | Everyone | Allowes access to use /emoji [list] |
| chatemojis.use.* | OP | Permission to use all emojis |
| chatemojis.reload | OP | Allowes access to reload emojis |
| chatemojis.admin | OP | Allowes access to change plugin settings |
| chatemojis.* | OP | Permission to use all emojis and /emoji command |

**Emoji-Specific Permission**:
Ungrouped emojis permission is as easy as `chatemojis.use.<name>`
Grouped emojis permission needs to include the path to the group (ex. `chatemojis.use.<group-path>.<name>`)
If you still don't understand how to get emoji-specific permission- as a server operator you're able to hover over the emoji (in the `/emoji` list) to view the permission node for that specific emoji.

# Dependencies
ChatEmojis does not hard-depend on any other plugins; Meaning **none** of these dependencies are required for ChatEmojis to work.
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** is used for placeholder parsing.
- **[EssentialsX](https://www.spigotmc.org/resources/essentialsx.9089/)** is used for parsing emoticons in direct messages.

Please note that if you are using PlaceholderAPI, you must also install the PlaceholderAPI Extension corresponding to the placeholder you're trying to access, more about this can be found [here](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders).

# How to create your own Emoji
A full tutorial and detailed explanation on how to create your own emoji can be found [here](https://github.com/Mxlvin/ChatEmojis/wiki/How-to-create-your-own-emoji).

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
