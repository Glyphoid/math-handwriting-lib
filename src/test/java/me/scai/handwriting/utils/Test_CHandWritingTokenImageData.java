package me.scai.handwriting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Test_CHandWritingTokenImageData {
  static final float floatTol = 1e-3F;

  @Test
  public void testReadImFileNoSourceEqualSeparator() {
    final String testResourcesPath = "test" + File.separator + "resources";
    final URL imUrl = this.getClass().getClassLoader().getResource(testResourcesPath +
                File.separator + "tokens" + File.separator +
                "no_source_equal_separator.im");

    File f = null;
    try {
      f = File.createTempFile("test", ".im");
      FileUtils.copyURLToFile(imUrl, f);

      CHandWritingTokenImageData imData = CHandWritingTokenImageData.readImFile(
          f, false, true, true);

      assertEquals("a", imData.tokenName);
      assertEquals(16, imData.nw);
      assertEquals(16, imData.nh);
      assertEquals(122.77209, imData.w, floatTol);
      assertEquals(119.35361, imData.h, floatTol);
      assertEquals(1, imData.nStrokes);
      assertEquals(2 + 16 * 16, imData.imData.length);
    } catch (IOException e) {
      fail("Failed due to IOException: " + e.toString());
    } finally {
      f.delete();
    }
  }

  @Test
  public void testReadImFileWithSourceColonSeparator() {
    final String testResourcesPath = "test" + File.separator + "resources";
    final URL imUrl = this.getClass().getClassLoader().getResource(testResourcesPath +
                File.separator + "tokens" + File.separator +
                "with_source_colon_separator.im");

    File f = null;
    try {
      f = File.createTempFile("test2", ".im");
      FileUtils.copyURLToFile(imUrl, f);

      CHandWritingTokenImageData imData = CHandWritingTokenImageData.readImFile(
          f, false, true, true);

      assertEquals("不", imData.tokenName);
      assertEquals(70, imData.nw);
      assertEquals(70, imData.nh);
      assertEquals(286.0, imData.w, floatTol);
      assertEquals(276.0, imData.h, floatTol);
      assertEquals(4, imData.nStrokes);
      assertEquals(2 + 70 * 70, imData.imData.length);
    } catch (IOException e) {
      fail("Failed due to IOException: " + e.toString());
    } finally {
      f.delete();
    }
  }
}