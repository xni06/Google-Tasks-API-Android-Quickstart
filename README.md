
## Google Tasks _default_ list Scrubber!
### What is it
Scrubs all deleted tasks in a given list having first moved all completed tasks there too - helps address [this](https://productforums.google.com/d/msg/gmail/yKbRwhi6hYU/2QXGlAfwQx4J) issue.

You can see your existing tasks [here](https://mail.google.com/tasks/canvas?pli=1) - remember to look in the _trash_!

It is based on the [original quickstart](https://github.com/xni06/Google-Tasks-API-Android-Quickstart) branch but has been [modified](https://github.com/xni06/Google-Tasks-API-Android-Quickstart/commit/fee0b65f3be3fd28fa88785b19f6ff1ce3494bee) so that it:

For a given task list...
1. Iterates through completed tasks and moves to bin, or
2. as above but also scrubs the task name and contents, including the bin

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
