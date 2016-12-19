# PlotGenerator
This Bukkit plugin provides a custom world generator which you can use with plugins like [Multiverse](https://github.com/Multiverse/Multiverse-Core/) to generate a world by repeating a MCEdit schematic. This can be really useful if you want to generate a plot world which this plugin is intended to do in the first place.

It has the ability to automatically protect created plots with [WorldGuard](https://github.com/sk89q/WorldGuard/) and you can automatically set the `BUYABLE` and `PRICE` flag either to use with your own region economy plugin or to buy it with the inbuild `/buyplot` command (needs [Vault](https://github.com/MilkBowl/Vault)). The teleport flag is also set to the south border of the region as an added feature, allowing you to teleport to it with the `/rg tp <id>` command!

There are two ways to setup the generator: Via the generator id (`-g PlotGenerator:testschematic,regionId=plot_%number%` in MultiVerse) or a config setting for the world and using just `-g PlotGenerator` as the world's generator.

## Setup via Generator ID

You can define all settings of the generator directly via the id string of the generator. This will override any settings made for that world in your config!

To create the same worlds as displayed below in the config section you would use the following commands (in Multiverse):

`/mv create test NORMAL -g PlotGenerator:testschematic,overlap=1,centerX=100,centerZ=0,regionId=%world%_plot_%number%,regionInset=5,regionMinY=40,regionMaxY=100,regionPrice=50`

`/mv create mb_plotworld NORMAL -g PlotGenerator:config=test,schem=plot` (You need to have the test section of the config defined, it wont use the generator of the test world!)

As you can see you just use `PlotGenerator:schematicname` without any special variable handling if you only want to repeat a schematic.

## Setup via Config

Define a section for your world in your config and then just set the `PlotGenerator`as the world's generator. (E.g. with `/mv create plot NORMAL -g PlotGenerator` in MultiVerse) Entries here do not overwrite the world's generator settings set via the ID.

```yaml
worlds:
  test: # config for world test
    schematic: testschematic # The name of the schematic
    overlap: 1 # How much it should overlap (e.g. if you want odd roads)
    center: # Where to start the schematics if you want to align it to some existin structures
      x: 100
      z: 0 # Can be ommited, defaults to zero
    region: # WorldGuard region creation settings
      id: %world%_plot_%number% # The id of the newly created region. %world% gets replaced with the world's name, %number% with the number of that region
      inset: 5 # How far away from the schematics border the region should start, use 0 to protect the whole schematic
      min-y: 40 # The lower end of the region
      max-y: 100 # The upper end of the region
      price: 50 # Sets the buyable and the price flag
  mb_plotworld:
    config: test # Use the settings of world test
    schematic: plot # but use the schematic plot
    
```

## Commands
Command                     | Permission                        | Explanation
----------------------------|-----------------------------------|-------------------------------------------------------
`/plotgenerator`            | `plotgenerator.command`           | Main plugin command, alias: /plotgen
`/plotgenerator reload`     | `plotgenerator.command.reload`    | Reloads the config You may need to unload the world or restart your server for the changes to take effect!
`/plotgenerator buy [<id>]` | `plotgenerator.command.buy`       | Short: `/buyplot [<regionid>]`. Purchase the region that you are standing in (needs Vault). If you have the permission `plotgenerator.command.buy.byregionid` you can specify a region id.

## Downloads

[Dev builds](https://ci.minebench.de/job/PlotGenerator/) can be found on the Minebench jenkins server.
