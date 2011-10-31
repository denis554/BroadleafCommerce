/*
 * Copyright 2008-2009 the original author or authors.
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
package org.broadleafcommerce.common.locale.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.locale.dao.LocaleDao;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by bpolster.
 */
@Service("blLocaleService")
public class LocaleServiceImpl implements LocaleService {
    private static final Log LOG = LogFactory.getLog(LocaleServiceImpl.class);

    @Resource(name="blLocaleDao")
    protected LocaleDao localeDao;


    /**
     * @return The locale for the passed in code
     */
    @Override
    public Locale findLocaleByCode(String localeCode) {
        return localeDao.findLocaleByCode(localeCode);
    }

    /**
     * Returns the page template with the passed in id.
     *
     * @param id - the id of the page template
     * @return The default locale
     */
    @Override
    public Locale findDefaultLocale() {
        return localeDao.findDefaultLocale();
    }


    /**
     * Returns list of supported locales.
     */
    @Override
    public List<Locale> findAllLocales() {
        return localeDao.findAllLocales();
    }
}
