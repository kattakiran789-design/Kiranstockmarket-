# 🚀 Telugu Stock Pro

A professional Android stock market trading analysis app with a TradingView-like dark theme, built with Kotlin and Jetpack Compose.

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Theme](https://img.shields.io/badge/Theme-Dark%20(TradingView)-gray)

---

## ✨ Features

### 📊 Home Screen
- Live market indices cards (NIFTY 50, SENSEX, BANK NIFTY, NIFTY IT)
- Quick watchlist with real-time prices
- Top 10 Gainers and Losers sections
- Market movers and sector performance
- Smooth pull-to-refresh

### 📈 Markets Screen
- Advanced stock search with instant results
- Filter chips: All, Gainers, Losers, Most Active
- Stock list with price, change %, and volume
- Sector tags for each stock

### 🕯️ Chart Screen (Most Important!)
- **Full-screen Candlestick Chart** (Red/Green candles)
- Zoom, Pan, Pinch gesture support
- Crosshair with price/time tracking
- **Technical Indicators:**
  - Moving Averages (EMA 9, 21, 50)
  - Bollinger Bands
  - RSI, MACD (coming soon)
  - Volume bars
- Multiple time periods: 1D, 1W, 1M, 3M, 6M, 1Y, ALL
- Option Chain summary for Nifty/Bank Nifty
- Quick BUY/SELL buttons

### 🧠 Analysis Screen
- AI-powered market direction (Bullish/Bearish/Sideways)
- Confidence percentage meter
- Probability of upside/downside
- Technical indicators summary
- Support & Resistance levels
- Trading suggestions with Entry, Target, Stop Loss, Risk:Reward

### 🎨 Design
- Professional dark theme inspired by TradingView
- Smooth animations and transitions
- Material 3 components
- Responsive layout for all screen sizes

---

## 📁 Project Structure

```
telugu-stock-pro/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/telugustockpro/
│   │       │   ├── TeluguStockApp.kt          # Application class
│   │       │   ├── MainActivity.kt            # Main activity
│   │       │   ├── data/
│   │       │   │   ├── model/
│   │       │   │   │   └── StockModels.kt     # Data models
│   │       │   │   ├── repository/
│   │       │   │   │   └── SampleData.kt      # Sample data & generators
│   │       │   │   └── remote/
│   │       │   │       └── StockApiService.kt # Retrofit API interface
│   │       │   ├── di/
│   │       │   │   └── NetworkModule.kt       # Hilt DI module
│   │       │   ├── viewmodel/
│   │       │   │   └── StockViewModel.kt      # Main ViewModel
│   │       │   ├── ui/
│   │       │   │   ├── TeluguStockMainApp.kt  # Main app composable
│   │       │   │   ├── navigation/
│   │       │   │   │   └── NavGraph.kt        # Navigation setup
│   │       │   │   ├── theme/
│   │       │   │   │   ├── Color.kt           # TradingView colors
│   │       │   │   │   ├── Theme.kt           # Material 3 theme
│   │       │   │   │   └── Type.kt            # Typography
│   │       │   │   ├── components/
│   │       │   │   │   ├── CommonComponents.kt
│   │       │   │   │   └── CandlestickChart.kt
│   │       │   │   └── screens/
│   │       │   │       ├── home/HomeScreen.kt
│   │       │   │       ├── markets/MarketsScreen.kt
│   │       │   │       ├── chart/ChartScreen.kt
│   │       │   │       ├── analysis/AnalysisScreen.kt
│   │       │   │       └── splash/SplashScreen.kt
│   │       │   └── util/
│   │       │       └── Extensions.kt          # Utility extensions
│   │       └── res/
│   │           ├── values/
│   │           │   ├── strings.xml
│   │           │   └── themes.xml
│   │           └── drawable/
│   │               └── ic_launcher_foreground.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Design System | Material 3 |
| Architecture | MVVM |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Navigation | Navigation Compose |
| Image Loading | Coil |
| Charts | Custom Canvas implementation |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/telugu-stock-pro.git
   cd telugu-stock-pro
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project folder

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - If not, click "Sync Now" in the notification bar

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️)

---

## 📊 Adding More Indicators

The app is designed for easy extension. Here's how to add new indicators:

### Step 1: Add indicator calculation in `Extensions.kt`

```kotlin
fun List<Double>.calculateYourIndicator(period: Int): List<Double?> {
    // Your calculation logic here
    return mapIndexed { index, value ->
        if (index < period - 1) null
        else {
            // Calculate indicator value
            subList(index - period + 1, index + 1).average()
        }
    }
}
```

### Step 2: Add indicator toggle in `ChartScreen.kt`

```kotlin
// Add to the indicators list
val indicators = listOf(
    // ... existing indicators
    "YOUR_IND" to "Your Indicator"
)
```

### Step 3: Add rendering logic in `CandlestickChart.kt`

```kotlin
if (activeIndicators.contains("YOUR_IND")) {
    drawYourIndicator(
        candleData = candleData,
        color = YourColor,
        // ... other parameters
    )
}
```

---

## 🔌 Connecting to Real API

The app uses sample data by default. To connect to a real stock API:

1. **Update `StockApiService.kt`** with your API base URL
2. **Create a Repository class** to handle API calls
3. **Update `StockViewModel.kt** to use the repository
4. **Add your API key** to `local.properties`:

```properties
STOCK_API_KEY=your_api_key_here
```

### Recommended APIs for Indian Stock Market:
- [NSE India API](https://www.nseindia.com)
- [Yahoo Finance](https://finance.yahoo.com)
- [Alpha Vantage](https://www.alphavantage.co)
- [Financial Modeling Prep](https://financialmodelingprep.com)
- [StockData.org](https://stockdata.org)

---

## 🎨 Customization

### Changing Colors
Edit `Color.kt` to customize the theme:

```kotlin
object TradingViewColors {
    val Green = Color(0xFF26A69A)      // Bullish color
    val Red = Color(0xFFEF5350)        // Bearish color
    val Background = Color(0xFF0D1117)  // App background
    // ... more colors
}
```

### Adding New Screens
1. Create a new composable in `ui/screens/`
2. Add route in `NavGraph.kt`
3. Add navigation option in `TeluguStockMainApp.kt`

---

## 📱 Screenshots

| Home | Markets | Chart | Analysis |
|------|---------|-------|----------|
| ![Home](https://via.placeholder.com/300x600/0D1117/58A6FF?text=Home) | ![Markets](https://via.placeholder.com/300x600/0D1117/58A6FF?text=Markets) | ![Chart](https://via.placeholder.com/300x600/0D1117/58A6FF?text=Chart) | ![Analysis](https://via.placeholder.com/300x600/0D1117/58A6FF?text=Analysis) |

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Inspired by [TradingView](https://www.tradingview.com) for the dark theme design
- Built with modern Android development practices
- Indian stock market data structure (NSE/BSE)

---

## 📞 Contact

**Your Name** - your.email@example.com

Project Link: [https://github.com/yourusername/telugu-stock-pro](https://github.com/yourusername/telugu-stock-pro)

---

**Made with ❤️ for Indian Stock Market Traders**
