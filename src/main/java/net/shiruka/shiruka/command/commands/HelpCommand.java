/*
 * MIT License
 *
 * Copyright (c) 2021 Shiru ka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.shiruka.shiruka.command.commands;

import static net.shiruka.api.command.CommandResult.of;
import net.shiruka.api.command.builder.LiteralBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents help command.
 */
public final class HelpCommand extends CommandHelper {

  /**
   * ctor.
   */
  public HelpCommand() {
    super("help", "Shows the help menu", "shiruka.command.help");
  }

  /**
   * registers the stop command.
   */
  public static void init() {
    new HelpCommand().register();
  }

  @NotNull
  @Override
  protected LiteralBuilder build() {
    return super.build()
      .executes(context -> of());
  }
}
