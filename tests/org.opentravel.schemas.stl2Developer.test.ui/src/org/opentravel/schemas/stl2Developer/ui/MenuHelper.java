/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.stl2Developer.ui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

/**
 * @author Pawel Jedruch
 * 
 */
public class MenuHelper {

    public static void saveAll(SWTWorkbenchBot bot) {

    }

    public static void openLibrary(SWTWorkbenchBot bot, File file) {
        bot.menu("Library").menu("Open...").click();

        // TODO workaround to handle native widgets. It can be done differently using factory method
        // to create different widgets base on test/production mode.
        bot.sleep(1000);
        // hack (need to be done when layout is PL)
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";

        // type in path to file
        for (char c : file.getAbsolutePath().toCharArray()) {
            bot.activeShell().pressShortcut(SWT.NONE, c);
        }
        bot.activeShell().pressShortcut(Keystrokes.LF);
    }

}
