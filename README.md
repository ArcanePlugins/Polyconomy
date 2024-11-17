<div align="center">

# Polyconomy

The *ultimate* virtual economy service provider for Minecraft servers and proxies.

It's reliable, robust, powerful, performant, featureful, and free.

***

<h4 id="quick-links">

• [Documentation](https://github.com/ArcanePlugins/Polyconomy/wiki)
• [Discord (Support & Discussion)](https://discord.gg/HqZwdcJ)
•

• [To-Do List](https://github.com/orgs/ArcanePlugins/projects/10/views/1
• [Source Code](https://github.com/ArcanePlugins/Polyconomy)
• [Issue Tracker](https://github.com/ArcanePlugins/Polyconomy/issues)
•

</h4>

***

## ⚠️ Pre-Alpha Notice ⚠️

Polyconomy is currently in very early development. It is not usable in its current state. **Most of the links in this
file are non-functional!**

We're slowly but surely building up a to-do list of what needs to be
done [here](https://github.com/orgs/ArcanePlugins/projects/10/views/1).

You are highly encouraged to discuss and partake in Polyconomy's development: join
the [ArcanePlugins Discord guild](https://discord.gg/HqZwdcJ). Please select 'other lokka30 plugins' role on join and
use the channel under the same name to chat.

</div>

***

## About

### What is Polyconomy's purpose?

Polyconomy is an economy service provider plugin, meaning that it controls the core component of your Minecraft
server/proxy's virtual economy. Polyconomy's primary job is to provide the economy service with mostly 'back-end'
features such as managing accounts, balances, currencies, and other things which are core to your economy. The only
user-facing features are basic commands such as `/pay`, `/bal`, and `/baltop`. The rest of the features you would expect
in your virtual economy are handled by other plugins, such as [jobs]() and [shops](), which give instructions to
Polyconomy such as *'give Notch 35.50 dollars'*.

To understand Polyconomy's role, you could separate your virtual economy into three parts:

- **Economy Service Providers** ([Polyconomy](), etc)
    - ...providing the backbone of the economy, managing all of its data. In most cases, only one Provider is present.
- **Economy Service APIs** ([Treasury](), [Vault](), etc)
    - ...providing the interface between Providers and Consumers, like a special common language for plugins to talk to
      each other. In most cases, only one API is present.
- **Economy Service Consumers** ([jobs](), [shops](), etc)
    - ...providing ways to interact with the economy - earning and spending money. In most cases, multiple Consumers are
      present.

All three of these components are required for a complete virtual economy.

### There are already economy service provider plugins - so why another?

Polyconomy was founded simply due to the previous lack of [economy service provider]() plugins which fit the community's
demands.

Polyconomy sports full [Treasury]() and [Vault]() API support, meaning that almost every single plugin which uses those
APIs are able to talk to Polyconomy with no problem. Although we recommend you use [Treasury](), Polyconomy is one of
the few plugins which supports Vault's bank feature, and is able to translate deprecated Vault API calls for player and
non-player contexts, making Polyconomy compatible with plugins such as [TownyAdvanced]()
and [GriefDefender](). [Native multi-currency support]() is one of the features unlocked by using [Treasury](), allowing
you to centralise all of your virtual economies under a single roof without using additional economy plugins.

When dealing with any economy, virtual or not, it's imperative that it is managed reliably. Other economy plugins may
fail during the course of a production server and cause great damage to their communities. To achieve Polyconomy's
reliability, it has been developed since day one with robustness being paramount. Only features which the plugin
strictly need are added; auxilary features are refused as to not damage the strong integrity of the software. We
carefully design and implement every feature so it fits in the plugin like a puzzle. Every piece fits in and
accomplishes a goal which is desired by a substantial amount of the plugin's users. In addition to optimisation efforts,
our scrutiny for the plugin's features make it as light as a feather to run on your servers and proxies.

Polyconomy is fully compatible and supported with a [variety of server and proxy platforms](). Even [Minestom]()!

You'll feel 'at home' with familiar commands like `/bal`, `/pay`, `/baltop`, `/eco`, and so on. You and your players
won't have to learn unnecessarily renamed commands. And where you are confused about anything,
there's [excellent documentation]() and volunteer support helpers on our [Discord guild]() who are happy to help.

Polyconomy is libre software, licensed under [GNU AGPL v3](LICENSE.md), which serves and protects your freedoms, unlike
proprietary software which takes them away. Polyconomy is a community project; you are strongly encouraged to discuss
and partake in the development of the plugin - embrace the open-source spirit!

## Installation

**Before you install Polyconomy, [make sure your server/proxy is compatible]().**
Then, follow the [super simple installation guide](). :)

## License

[![GNU AGPL v3 Free Software](https://www.gnu.org/graphics/agplv3-88x31.png)](https://www.gnu.org/licenses/agpl-3.0.html)

By using the software, you accept the [license](LICENSE.md).
