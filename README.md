
## Delete Google Tasks from your _default_ list
### What is it
Helps facilitate the need to move tasks from the default list to another so that they can be deleted as mentioned in [this](https://productforums.google.com/d/msg/gmail/yKbRwhi6hYU/2QXGlAfwQx4J) posting.

You can see your existing tasks [here](https://mail.google.com/tasks/canvas?pli=1) - remember to look in the _trash_!

It is based on the [original quickstart](/xni06/Google-Tasks-API-Android-Quickstart/tree/master) branch but has been [modified](/xni06/Google-Tasks-API-Android-Quickstart/tree/move-tasks-from-default-list/commit/fee0b65f3be3fd28fa88785b19f6ff1ce3494bee) so that it:

1. Copies up to 100 tasks (default max results) individually from the _default_ list to the destination list called `movedTasks`
1. Deletes the copied tasks from default list individually

### Usage
1. Press the big button at the top of the screen until all tasks have been moved!
1. Once all the tasks have been _moved_ from your _default_ list, you can then delete the `movedTasks` list.

### Testing
It has been tested on the following devices:

|Emulator|Nexus 5|Google Pixel|
|---|---|---|
|OK|?|?|

### Release APK
The release APK can be found [here](app-release.apk) to save you having to set up the keys and building yourself.

### Notes
1. this has been created to move my +3000 tasks from my default list (previously in the _trash_) so that they can disappear for eternity - it did the job perfectly though it did take a while to run having had to press the big button 30 times or more
1. if you receive an _out of quota_ message, just try again
1. if you receive any other error messages, just try again
1. you may end up with only 1 task remaining - take a look at that task as it may simply be an empty task
1. feel free to contribute
1. use at your own risk!


### References
1. https://productforums.google.com/forum/#!topic/calendar/ev18sUtfloY;context-place=topicsearchin/calendar/tasks$2030$20days
1. https://productforums.google.com/forum/#!topic/gmail/yKbRwhi6hYU
1. https://productforums.google.com/forum/#!searchin/calendar/deleted$20tasks/calendar/cDqKcfyNjaQ
1. https://productforums.google.com/forum/#!msg/gmail/yKbRwhi6hYU/2QXGlAfwQx4J
1. https://productforums.google.com/forum/#!topic/calendar/sKzX2r3IV2o
1. https://productforums.google.com/forum/#!topic/gmail/8wL03Pxh5xk
1. http://webapps.stackexchange.com/questions/50440/how-to-delete-completed-tasks-in-google-tasks
1. https://mail.google.com/tasks/canvas?pli=1
1. http://webapps.stackexchange.com/a/66077


===

Portions of this page are modifications based on work created and [shared by Google](https://developers.google.com/readme/policies/) and used according to terms described in the [Creative Commons 3.0 Attribution License](http://creativecommons.org/licenses/by/3.0/).
