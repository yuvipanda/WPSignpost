from db import *
from flask import *
import json

setup_all()

app = Flask(__name__)
app.debug = True

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
    issue = Issue.query.order_by(Issue.date.desc()).slice(0, 1)[0]
    return (json.dumps(issue.serialize()), 200, {'Content-Type': 'application/json'})

@app.route('/posts/<int:issue_id>')
def posts(issue_id):
    issue = Issue.query.filter_by(id=issue_id).one()
    data = [post.serialize(True) for post in issue.posts]
    return (json.dumps(data), 200, {'Content-Type': 'application/json'})

@app.route('/post/<int:post_id>')
def post(post_id):
    post = Post.query.filter_by(id=post_id).one()
    return (json.dumps(post.serialize()), 200, {'Content-Type': 'application/json'})

@app.route('/post/permalink/<path:permalink>')
def post_permalink(permalink):
    print permalink
    post = Post.query.filter_by(permalink=permalink).one()
    return (json.dumps(post.serialize()), 200, {'Content-Type': 'application/json'})

@app.route('/posts/permalink/<path:permalink>')
def posts_permalink(permalink):
    issue = Issue.query.filter_by(permalink=permalink).one()
    # Hack to make the order make more sense in client. FIXME: Make this more deterministic
    data = reversed([post.serialize(True) for post in issue.posts])
    return (json.dumps(data), 200, {'Content-Type': 'application/json'})

@app.route('/issue/permalink/<path:permalink>')
def issue_permalink(permalink):
    issue = Issue.query.filter_by(permalink=permalink).one()
    posts = reversed([post.serialize(True) for post in issue.posts])
    data = issue.serialize()
    data['posts'] = posts
    return (json.dumps(data), 200, {'Content-Type': 'application/json'})

if __name__ == '__main__':
    app.run()
