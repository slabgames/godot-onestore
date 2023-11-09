extends Node

var _onestore = null

func _ready():
    if Engine.has_singleton("GodotOneStore"):
        _onestore = Engine.get_singleton("GodotOneStore")
    else:
        push_warning('Adjust plugin not found!')
    if ProjectSettings.has_setting('OneStore/AppToken'):
        var token = ProjectSettings.get_setting('OneStore/AppToken')
        init(token, not OS.is_debug_build())
    else:
        push_error('You should set OneStore/AppToken to SDK initialization')

func init(token: String, production := false) -> void:
    if _onestore != null:
        _onestore.init(token, production)
        print('OneStore plugin inited!')
        