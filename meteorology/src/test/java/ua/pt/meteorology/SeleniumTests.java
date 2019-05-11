/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pt.meteorology;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
/**
 *
 * @author dn
 */
public class SeleniumTests {
    private WebDriver driver;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
      driver = new FirefoxDriver();
      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void testAveiro3Days() throws Exception {
      driver.get("http://localhost:8080/meteorology");
      driver.findElement(By.id("select_city")).click();
      new Select(driver.findElement(By.id("select_city"))).selectByVisibleText("Aveiro");
      driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='CHOOSE CITY :'])[1]/following::option[2]")).click();
      driver.findElement(By.id("start_day")).click();
      new Select(driver.findElement(By.id("start_day"))).selectByIndex(1);
      driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='START DAY :'])[1]/following::option[2]")).click();
      String first_day = new Select(driver.findElement(By.id("start_day"))).getFirstSelectedOption().getText();
      driver.findElement(By.id("end_day")).click();
      new Select(driver.findElement(By.id("end_day"))).selectByIndex(3);
      driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='END DAY :'])[1]/following::option[3]")).click();
      String end_day = new Select(driver.findElement(By.id("end_day"))).getFirstSelectedOption().getText();
      driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='END DAY :'])[1]/following::b[1]")).click();
      assertEquals("Aveiro", driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Get Previsions'])[1]/following::b[1]")).getText());
      assertEquals(first_day, driver.findElement(By.xpath("//div[@id='meteorology_div']/div/h4/b")).getText());
      assertEquals(end_day, driver.findElement(By.xpath("//div[@id='meteorology_div']/div[3]/h4/b")).getText());
    }

    @Test
    public void testPorto5days() throws Exception {
      driver.get("http://localhost:8080/meteorology");
      driver.findElement(By.id("select_city")).click();
      new Select(driver.findElement(By.id("select_city"))).selectByVisibleText("Porto");
      String first_day = new Select(driver.findElement(By.id("start_day"))).getFirstSelectedOption().getText();
      String end_day = new Select(driver.findElement(By.id("end_day"))).getFirstSelectedOption().getText();
      driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='END DAY :'])[1]/following::b[1]")).click();
      assertEquals("Porto", driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Get Previsions'])[1]/following::b[1]")).getText());
      assertEquals(first_day, driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Porto'])[2]/following::b[1]")).getText());
      assertEquals(end_day, driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='MODERATE (E)'])[3]/following::b[2]")).getText());
    }

    @After
    public void tearDown() throws Exception {
      driver.quit();
      String verificationErrorString = verificationErrors.toString();
      if (!"".equals(verificationErrorString)) {
        fail(verificationErrorString);
      }
    }
  }