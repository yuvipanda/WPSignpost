#!/data/project/wp-signpost/code/signpost/bin/python
import site
site.addsitedir("/data/project/wp-signpost/code/signpost/lib/python2.7/site-packages")

from wsgiref.handlers import CGIHandler
from werkzeug.debug import DebuggedApplication
from app import app

app.debug = True
CGIHandler().run(DebuggedApplication(app))
