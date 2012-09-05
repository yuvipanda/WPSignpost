from db import *
from flask import *
import json
from werkzeug.contrib.cache import MemcachedCache
app = Flask(__name__)
app.debug = True

setup_app(app)

cache = MemcachedCache(['127.0.0.1:11211']) 

@app.route('/issues')
def firstIssues():
    return issues(0)

@app.route('/issues/<int:offset>')
def issues(offset=0):
    issues = Issue.query.order_by(Issue.date.desc()).offset(offset)
    data = [issue.serialize() for issue in issues]
    return (json.dumps(data), 200, {'Content-Type': 'application/json'})

@app.route('/issue/latest')
def latest_issue():
    issue = cache.get("latest_issue")
    if not issue:
        issue = Issue.query.order_by(Issue.date.desc()).limit(1).one()
    return (json.dumps(issue.serialize()), 200, {'Content-Type': 'application/json'})

@app.route('/post/permalink/<path:permalink>')
def post_permalink(permalink):
    post = Post.query.filter_by(permalink=permalink).one()
    return (json.dumps(post.serialize()), 200, {'Content-Type': 'application/json'})

@app.route('/issue/permalink/<path:permalink>')
def issue_permalink(permalink):
    issue = Issue.query.filter_by(permalink=permalink).one()
    posts = [post.serialize(True) for post in issue.posts]
    data = issue.serialize()
    data['posts'] = posts
    return (json.dumps(data), 200, {'Content-Type': 'application/json'})

@app.route('/issue/update/<date>', methods=["POST"])
def update_issue(date):
    from scraper import save_issue
    return save_issue(date)

@app.route('/issue/push/latest', methods=["POST"])
def push_latest_issue():
    issue = Issue.query.order_by(Issue.date.desc()).limit(1).one()
    from push import push_issue
    return str(push_issue(issue.permalink))

@app.route('/device/register', methods=["POST"])
def register_device():
    regID = request.form['regID']
    if regID:
        if Device.query.filter_by(regID=regID).count() == 0:
            device = Device(regID=regID)
            db.session.add(device)
            db.session.commit() 
            return ("", 200)
        else:
            return ("regID already registered", 200)
    else:
        return ("No regID specified", 400) 

@app.route('/device/deregister', methods=["POST"])
def deregister_device():
    regID = request.form['regID']
    if regID:
        if Device.query.filter_by(regID=regID).count() != 0:
            device = Device.query.filter_by(regID=regID).one()
            db.session.delete(device)
            db.session.commit() 
        return ("", 200)
    else:
        return ("No regID specified", 400) 

if __name__ == '__main__':
    app.run()
