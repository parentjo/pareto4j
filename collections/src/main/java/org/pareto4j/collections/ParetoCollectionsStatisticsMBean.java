package org.pareto4j.collections;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 2/05/13
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
public interface ParetoCollectionsStatisticsMBean {
    long getListTotal();

    long getListExpands();

    long getListEmpty();

    long getListOne();

    long getListGeneral();

    String getListPercentage();


    long getMapTotal();

    long getMapExpands();

    long getMapEmpty();

    long getMapOne();

    long getMapGeneral();

    String getMapPercentage();


    long getSetTotal();

    long getSetEmpty();

    long getSetOne();

    long getSetGeneral();


    String getSetPercentage();
}
