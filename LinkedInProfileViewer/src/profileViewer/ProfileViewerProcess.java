package profileViewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProfileViewerProcess implements Runnable {

	public String userName;
	public String password;
	public String leadsExcelPath;
	public String sheetName;
	public String columnNumber;
	public Workbook workbook;

	private WebDriver driver;

	public void startViewer(String userName, String password, String leadsExcelPath, String sheetName,
			String columnNumber) {
		try {

			File leads = new File(leadsExcelPath);
			String driverExecutablePath = Paths.get(System.getProperty("user.dir"), "driver", "chromedriver.exe")
					.toString();
			System.setProperty("webdriver.chrome.driver", driverExecutablePath);

			workbook = new XSSFWorkbook(leads);
			Sheet sheet = workbook.getSheet(sheetName);
			int lastRow = sheet.getLastRowNum();

			driver = new ChromeDriver();
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
			driver.manage().window().maximize();

			if (!signin(userName, password)) {
				workbook.close();
				driver.quit();
				return;
			}

			int counter = 0;

			for (int i = 0; i <= lastRow; i++) {
				Row row = sheet.getRow(i);
				String link;
				try {
					link = row.getCell(Integer.parseInt(columnNumber)).getStringCellValue();
					driver.get(link);
					waitForPageLoad(30);
					int randomScroll = getRandomNumber(4, 6);
					for (int j = 0; j < randomScroll; j++) {
						waitAnonymously(getRandomNumber(10, 15));
						scrollTo(Position.BOTTOM);
						waitAnonymously(getRandomNumber(10, 15));
						scrollTo(Position.TOP);
					}
					counter++;
					ProfileViewerConsole.log.append("\nCompleted " + counter + " out of " + (lastRow + 1));
				} catch (Exception e) {
					// Exception due to blank/empty row/cell. Can be ignored.
				}
			}

			workbook.close();
			driver.quit();
		} catch (InvalidFormatException e) {
			ProfileViewerConsole.log.append("\nInvalid Excel format.\nError Message: " + e.getMessage());
		} catch (IOException e) {
			ProfileViewerConsole.log
					.append("\nError while trying to read from the excel file.\nError Message: " + e.getMessage());
		} catch (Exception e) {
			ProfileViewerConsole.log
					.append("\nError while trying to start profile viewer.\nError Message: " + e.getMessage());
		}
	}

	public boolean signin(String username, String password) {
		try {
			By emailTxt = By.id("login-email");
			By passwordTxt = By.id("login-password");
			By signinBtn = By.id("login-submit");
			By homeIcon = By.id("feed-nav-item");

			driver.get("https://www.linkedin.com");
			driver.findElement(emailTxt).clear();
			driver.findElement(emailTxt).sendKeys(username);
			driver.findElement(passwordTxt).clear();
			driver.findElement(passwordTxt).sendKeys(password);
			driver.findElement(signinBtn).click();
			waitForPageLoad(30);

			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(homeIcon));

			return true;
		} catch (Exception e) {
			ProfileViewerConsole.log
					.append("\nError while trying to login into LinkedIn.\n Error Message: " + e.getMessage());
			return false;
		}
	}

	public void waitForPageLoad(int waitDurationInSec) {
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString()
						.equals("complete");
			}
		};
		try {
			Thread.sleep(1000);
			WebDriverWait wait = new WebDriverWait(driver, waitDurationInSec);
			wait.until(expectation);
		} catch (Throwable e) {
			ProfileViewerConsole.log
					.append("\nError while trying wait for the page to load.\n Error Message: " + e.getMessage());
		}
	}

	public void scrollTo(Position position) {
		try {
			switch (position) {
			case TOP:
				((JavascriptExecutor) driver).executeScript(
						"window.scrollBy({top: -document.body.scrollHeight,left: 0,behavior: 'smooth'});");
				break;
			case BOTTOM:
				((JavascriptExecutor) driver).executeScript(
						"window.scrollBy({top: document.body.scrollHeight,left: 0,behavior: 'smooth'});");
				break;
			default:
				break;
			}
		} catch (Exception e) {
			ProfileViewerConsole.log
					.append("\nError while trying to scroll to page bottom.\n Error Message: " + e.getMessage());
		}
	}

	public int getRandomNumber(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public void waitAnonymously(int waitTimeInSec) {
		try {
			Thread.sleep(waitTimeInSec * 1000);
		} catch (InterruptedException e) {
			ProfileViewerConsole.log.append("\nError while waiting anonymously.\n Error Message: " + e.getMessage());
		}
	}

	public void setValues(String userName, String password, String leadsExcelPath, String sheetName,
			String columnNumber) {
		this.userName = userName;
		this.password = password;
		this.leadsExcelPath = leadsExcelPath;
		this.sheetName = sheetName;
		this.columnNumber = columnNumber;
	}

	public void stopViewer() {
		try {
			if (driver != null) {
				driver.quit();
				workbook.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		startViewer(userName, password, leadsExcelPath, sheetName, columnNumber);
	}
}
