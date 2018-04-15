# GMChanges

Version 0.6
=========================
Added ScrollofTownPortal


Version 0.5
=========================
Fixed crating to HolyBook


Version 0.4 - 2018March26
=========================
minor bugfix to apply with version 1.6


Version 0.3 - 2018March25
=========================

Added commands
#fillup <player>
#sendhome <player>
#moveplayer <player>

fillup will fill players hunger, stamina and ccfp to max
sendhome teleports player to his/her token
moveplayer teleports player to your position



Version 0.2 - 2018March24
=========================


Issue 1- Commands not working with other mods using commands 
Changed handling of commands from a registerhook method to make the  main class AllInOne implement PlayerMessageListener

Commands are now handled within a onPlayerMessage method of AllInOne class

Cleanup of uncessary code in CmdTool class.



Issue 2- GM protect not persisted on fences and walls - 
Added calls to savePermissions in several action methods within GmProtect


