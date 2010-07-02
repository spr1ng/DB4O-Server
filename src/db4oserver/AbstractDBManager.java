/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package db4oserver;

import com.db4o.ObjectContainer;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author spr1ng
 * @version $Id$
 */
public abstract class AbstractDBManager {

    public static final Logger LOG = Logger.getAnonymousLogger();
    public abstract ObjectContainer getContainer();
    
    /**
     * Сохраняет все объекты списка в базу. Возвращает количество новых
     * сохраненных объектов
     * @param objects
     */
    public abstract int store(List objects);

    /**
     * Показывает сохранен ли данный объект в базе
     * @param o
     * @return
     */
    public boolean isItemStored(Object o) {
        ObjectContainer db = getContainer();
        try {
            if (db.queryByExample(o).size() > 0) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            LOG.severe(ex.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

}
