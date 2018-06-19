/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.awt.font;

import java.awt.font.TextAttribute;
import junit.framework.TestCase;

public class TextAttributeTest extends TestCase {

    public void testAttributeNames() {
        // J2ObjC reflection-stripping change.
        assertTrue(TextAttribute.KERNING.toString().matches(
                "[Jj]ava\\.?[Aa]wt\\.?[Ff]ont\\.?TextAttribute\\(kerning\\)"));
        assertTrue(TextAttribute.LIGATURES.toString().matches(
                "[Jj]ava\\.?[Aa]wt\\.?[Ff]ont\\.?TextAttribute\\(ligatures\\)"));
        assertTrue(TextAttribute.TRACKING.toString().matches(
                "[Jj]ava\\.?[Aa]wt\\.?[Ff]ont\\.?TextAttribute\\(tracking\\)"));
    }

    public void testAttributeValues() {
        assertEquals(new Integer(1), TextAttribute.KERNING_ON);
        assertEquals(new Integer(1), TextAttribute.LIGATURES_ON);
        assertEquals(new Float(0.04f), TextAttribute.TRACKING_LOOSE);
        assertEquals(new Float(-0.04f), TextAttribute.TRACKING_TIGHT);
    }
}
