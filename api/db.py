import json
import time

from elixir import *
import settings

metadata.bind = settings.DATABASE

class Post(Entity):
    using_options(tablename='posts')

    permalink = Field(Unicode(255))
    title = Field(Unicode(255))
    content = Field(UnicodeText)
    author = Field(Unicode(255))
    author_link = Field(Unicode(255))
    image_link = Field(Unicode(255))
    issue = ManyToOne('Issue')

    def serialize(self, exclude_content=False):
        data = {
            'id': self.id,
            'permalink': self.permalink,
            'title': self.title,
            'author_name': self.author,
            'author_link': self.author_link,
            'image_url': self.image_link
        }
        if not exclude_content:
            data['content'] = self.content
        return data

class Issue(Entity):
    using_options(tablename='issues')

    date = Field(DateTime)
    posts = OneToMany('Post')


    def serialize(self):
        return {
            'id': self.id,
            'date': time.mktime(self.date.timetuple())
        }
