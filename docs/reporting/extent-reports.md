[Extent-Reports](https://extentreports.com/) is the reporting tool of choice for QED. 
It is a proven, detailed reporting tool with good configuration options.

QED supports the use of other reporting tools by installing their listeners. 
The concept has been tried out with Allure reports, but this has been abandoned as Allure proved to be very opinionated and monolithic.
For example, it was possible to install a customised listener, which would react in a similar way
to reporting events as the Extent listener, but it turned out to be impossible to turn off the
default Allure listener. On top of that, it slowed down execution significantly.

However, hooks to add external reports are still available, so other reporting tools can 
still be added if required.

At this stage though, we consider ExtentReports to be the most mature, configurable and feature-rich option available. 

Reports for a system under test can be configured as follows in the associated config file:

```json
{
  "reporting": {
    "extent": {
      "theme": "DARK",
      "reportName": "mixed UI API report",
      "documentTitle": "UI API Report",
      "enableScreenshots": true
    }
  }
}
```