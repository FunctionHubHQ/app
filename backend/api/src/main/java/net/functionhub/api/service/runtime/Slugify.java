package net.functionhub.api.service.runtime;

/*************************************************************************************
* Copyright (C) 2001-2011 encuestame: system online surveys Copyright (C) 2011
* encuestame Development Team.
* Licensed under the Apache Software License version 2.0
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to  in writing,  software  distributed
* under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
* CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
* specific language governing permissions and limitations under the License.
*************************************************************************************/

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 7/26/23
 */
@Component
public class Slugify {
  /**
   * No latin pattern.
   */
  private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
  /**
   * Whitespace pattern.
   */
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

  /**
   * Convert the String input to a slug.
   */
  public String toSlug(String input) {
    if (ObjectUtils.isEmpty(input)) {
     return null;
    }
    String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
    String slug = NONLATIN.matcher(normalized).replaceAll("");
    return slug.toLowerCase(Locale.ENGLISH);
  }

  /**
   * Normalize a string
   * @param input
   * @return
   */
  private String normalize(final String input) {
    if (input == null || input.length() == 0)
      return "";
    return Normalizer.normalize(input, Form.NFD).replaceAll("[^\\p{ASCII}]", "");
  }
}