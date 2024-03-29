# Changelog

## [1.0.0]
### Deprecated
- Dropped support for IDE version <= 203.* due to scheduled API removal.  
  If you are using old IDE, please try to install gfm-advanced@0.0.9.

## [0.0.9]
### Changed
- Set maximum compatible IDE version to 211.*.  
  The code base of this plugin will be incompatible after IDE version >= 212.

## [0.0.8] - April 11, 2021
### Fixed
- Fixed an issue where IDE stops loading on startup.

## [0.0.7] - April 9, 2021
### Fixed
- Fixed an issue where IDE version limitation was not correctly removed.
- Fixed an issue where plugin option was not searchable.

## [0.0.6] - April 8, 2021
### Fixed
- Removed IDE's upper version limit  
  \*Because of this limit, plugin users were always required to wait  
  for plugin update. I'm really sorry about this.
- Updated inner intellij plugin version
- Improved stability

## [0.0.5] - Jan 24, 2021
### Fixed
- Fixed typos. (Github -> GitHub)
- Fixed wrong text at `use offilne parser` checkbox in settings.

## [0.0.4] - Jan 13, 2021
### Added
- Now compatible with IntelliJ 2020.3

### Fixed
- Fixed an issue where emoji was not rendered properly.

### Changed
- Options of this plugin are not searchable in Settings dialog for now  
  because of [this issue](https://youtrack.jetbrains.com/issue/KTIJ-782)

## [0.0.3] - Aug 25, 2020
### Fixed
- Fixed an issue where GfmA panel not showing current markdown when markdown text edited before the panel opened.

## [0.0.2] - Jul 12, 2020
### Added
- Show notification label when falling back to offline parser.
### Fixed
- Fixed an issue where offline parser did not properly parse and render some gfm syntax.

[1.0.0]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.9...v1.0.0
[0.0.9]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/Hinaser/gfm-advanced/compare/v0.0.1...v0.0.2