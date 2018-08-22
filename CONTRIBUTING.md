# Contributing
Feel free to contribute code to MickiNet. You can do it by forking the repository via Github and sending pull 
request with changes.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as 
possible. Also be sure that all tests are passing.

<br>

## Introduction
It's **very important** to mention that I didn't add my `google-services.json` file (from Firebase) to the repository. 
Why I didn't add it? Well, from the Firebase doc

> Firebase manages all of your API settings and credentials through a single configuration file. The file is named google-services.json 
on Android and GoogleService-Info.plist on iOS.

It seems to make sense to add it to a `.gitignore` and not include it in a public repo. This was discussed in [issue 26],
with more details on what with more details on what [google-services.json] contains. A project like [googlesamples/google-services] 
does have it [in its .gitignore] for instance. 

Although, this [thread] does mention
> For a library or open-source sample we do not include the JSON file because the intention is that users insert their own to point 
the code to their own backend. That's why you won't see JSON files in most of our Firebase repos on GitHub.

<br>

## Get Started
### Adding Firebase Crash Reporting
The images below shows you how to add crash reporting services to project in two main steps.

| [Step 1] | [Step 2] |
|:-:|:-:|
| ![Step 1] | ![Step 2] |

After that you are ready to build the project.

<br>

## Merge
Your commits will be merged to `master` branch under your name.

## Bug Reports & Feature Requests
* It's always better to be a good and easy going person rather than "I know how to do blabla" kind of person.
* It doesn't make a difference how many years of experience do you have, ask questions, find solutions, communicate, team up, 
follow guidelines (see below).
* It's always better to ask before you do a big amount of work. Usually PRs should be very focused and should fix specific 
issue or add one feature at a time.
* Please be patient as not all items will be tested immediately - remember, MickiNet is open source.
* Occasionally I'll close issues. Please feel free to re-open issues I've closed if there's something we've missed and they 
still need to be addressed.
* Do not hurry. Give a time, think about the right way to implement stuff. Get some coffee.
* But after all, I would love to see your changes! :+1: 

<br>

## Rules
1. Try to avoid compound variable names, method names, class names as much as you can; but make it clear to the *readers* 
that why this variable is standing for. If you're about using long variable name, try to break your scope into smaller scopes. 
2. Insert comments to your code.
3. Make comments clean, keep it short and meaningful.
4. Write clean and _human readable_ code.

<br>

## Branch names, git, process, etc.
1. Before you do _any_ work, create GitHub issue. Give a good explanation of the story you gonna work on. Show examples.
It can be obvious for you, but not for others. Save time one should spend understanding your changes. Add screenshots, pics if needed.
2.Always prefix your commits with `# AAA` where `AAA` is GitHub issue number. For example: `#111 Add blabla to something`. Keep in mind 
that `#` is usually comment symbol in Git. So you may want to use `git commit -m "#111 Add blabla to something"` shell command or 
configure your `~/.gitconfig`
3. Use right wording for your Git commits. Make them meaningful, short, use imperative mood ([see good explanation here]).
4. Use `#AAA` prefix when you create PR. PR subject line should reflect your changes, should be meaningful and short.
5. When work is completed, test your feature/bug, add screenshots confirming everything works as expected. 
It can be obvious for you, but here you want to save time for others. :) 



[issue 26]: <https://github.com/googlesamples/google-services/issues/26#issuecomment-168869043>
[google-services.json]: <https://developers.google.com/android/guides/google-services-plugin#processing_the_json_file>
[googlesamples/google-services]: <https://github.com/googlesamples/google-services>
[in its .gitignore]: <https://github.com/googlesamples/google-services/blob/0edda8fe963a9baf78f67de4e78311c33e38c397/.gitignore>
[thread]: <https://groups.google.com/forum/#!msg/firebase-talk/bamCgTDajkw/uVEJXjtiBwAJ>
[Step 1]: <https://github.com/mirjalal/MickiNet/blob/master/docs/imgs/step1.png>
[Step 2]: <https://github.com/mirjalal/MickiNet/blob/master/docs/imgs/step2.png>
[see good explanation here]: <https://chris.beams.io/posts/git-commit/>
