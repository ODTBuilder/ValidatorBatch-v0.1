# -*- coding: utf-8 -*-
"""
/***************************************************************************
 aDockWidget
                                 A QGIS plugin
 aa
                             -------------------
        begin                : 2018-02-23
        git sha              : $Format:%H$
        copyright            : (C) 2018 by aa
        email                : aaa
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
"""

import os

from PyQt4.QtCore import*
from PyQt4 import uic,QtGui
from PyQt4.QtGui import *
from qgis.utils import QGis
from qgis.core import *
from qgis.gui import *
import os.path
import collections
import subprocess
import sys
reload(sys)
sys.setdefaultencoding('utf-8')
from PyQt4 import QtGui, uic
from PyQt4.QtCore import pyqtSignal

FORM_CLASS, _ = uic.loadUiType(os.path.join(
    os.path.dirname(__file__), 'aa_dockwidget_base.ui'))


class aDockWidget(QtGui.QDockWidget, FORM_CLASS):

    closingPlugin = pyqtSignal()

    def __init__(self, iface, parent=None):
        """Constructor."""
        super(aDockWidget, self).__init__(parent)
        # Set up the user interface from Designer.
        # After setupUI you can access any designer object by doing
        # self.<objectname>, and you can use autoconnect slots - see
        # http://qt-project.org/doc/qt-4.8/designer-using-a-ui-file.html
        # #widgets-and-dialogs-with-auto-connect
        self.setupUi(self)
        self.iface = iface     #초기 세팅
        reload(sys)
        sys.setdefaultencoding('utf-8')


    def closeEvent(self, event):
        self.closingPlugin.emit()
        event.accept()

