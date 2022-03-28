# Patco Today
Patco Today is a free, open-source transit app for Android. Primarily utilizing the Google Transit Feed Specification (GTFS) API, it provides the most up-to-date scheduling information for PATCO Transit.

## Contribute code

Whether you’ve fixed a bug or introduced a new feature, pull requests are welcome! To help translate Patco Today, please see “[Translate](#translate).”

You can use Git to clone this repository:

```
git clone --recursive https://github.com/Davidp799/patco-today.git
```

To build the app, select the `lawnWithQuickstepDebug` build type. Should you face errors relating to the `iconloaderlib` and `searchuilib` projects, run `git submodule update --init --recursive`.

Here are a few contribution tips:

- [The `lawnchair` package](https://github.com/LawnchairLauncher/lawnchair/tree/12-dev/lawnchair) houses Lawnchair’s own code, whereas [the `src` package](https://github.com/LawnchairLauncher/lawnchair/tree/12-dev/src) includes a clone of the Launcher3 codebase with modifications. Generally, place new files in the former, keeping changes to the latter to a minimum.

- You can use either Java or, preferably, Kotlin.

- Make sure your code is logical and well formatted. If using Kotlin, see [“Coding conventions” in the Kotlin documentation](https://kotlinlang.org/docs/coding-conventions.html).

- Set `12-dev` as the base branch for pull requests.

## Translate

You can help translate Lawnchair 12 [on Crowdin](https://lawnchair.crowdin.com/lawnchair). Here are a few tips:

- When using quotation marks, insert the symbols specific to the target language, as listed in [this table](https://en.wikipedia.org/wiki/Quotation_mark#Summary_table).

- Lawnchair uses title case for some English UI text. Title case isn’t used in other languages; opt for sentence case instead.

- Some English terminology may have no commonly used equivalents in other languages. In such cases, use short descriptive phrases—for example, the equivalent of _bottom row_ for _dock_.

## Privacy Policy

[Privacy Policy](privacy-policy.md)

## Terms and Conditions

[Terms and Conditions](terms-conditions.md)

## License

This Privacy Policy Template is licensed under the GNU General Public License, version 3 (GPLv3) and is distributed free of charge.

## Quick links

- [News](https://t.me/lawnchairci)
- [Lawnchair on Twitter](https://twitter.com/lawnchairapp)
- [Website](https://lawnchair.app)
- [_XDA_ thread](https://forum.xda-developers.com/t/lawnchair-customizable-pixel-launcher.3627137/)
