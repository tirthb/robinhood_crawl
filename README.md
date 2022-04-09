Robinhood Crawl Bot for Portfolio CSV
=====================================


I like Robinhood and it's commission-free trading. But, there is no easy way to export my portfolio in Robinhood. 
So, I wrote this selenium bot to help me and others generate a tab delimited csv file representing our portfolio. The portfolio has addtional columns to help me analyze my portfolio.

I use this code regularly, so I will keep it updated and support it till Robinhood allows crawling the web version.

It will work for you if you just trade put credit spreads and long calls, like me. If you trade shares and other vehicles, please fork and feel free to modify it. Or connect me through github and I will be happy to give you write access.

Thanks to the `Selenium-Maven-Template` project from which I forked.

1. Open a terminal window/command prompt
2. Clone this project.
3. `cd robinhood_crawl` (Or whatever folder you cloned it into)
4. Create a file login.properties under src/test/resources.
5. Add your credentials to login.properties. I have added `*.properties` to `.gitignore` so we do not accidentlaly commit this file.
```
rh.name=your_username
rh.pzwd=your_password
```
6. `mvn -Dbrowser=firefox -Dheadless=false clean verify` This step will take a long time for the first time as it downloads all the binaries. Subsequently, it takes 10 mins for 50 positions.
7. The output file is portfolio.tsv under the main folder. It is a tab-delimited csv file.
[Sample](https://docs.google.com/spreadsheets/d/1JTCY-gocBJue6IZQL0WaQpdzBk42fxedXiUxsyUI9U4/edit?usp=sharing) portfolio export csv file.

### What should I know?

- You need git, maven and java to run this. If using mac, use brew to install these platforms.

### Known problems...

- It will only work for a portfolio with put credit spreads and long calls now.

### Anything else?

As you will be logging in for the first time in the bot browser window, Robinhood will ask you to verify your identity with a text message on your phone. Other github projects do not work as Robinhood requires this additional step to authenticate now.
