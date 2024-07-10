import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Session e2e test', () => {
  const sessionPageUrl = '/session';
  const sessionPageUrlPattern = new RegExp('/session(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const sessionSample = { startDate: '2023-11-16T13:12:50.532Z' };

  let session;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/sessions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/sessions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/sessions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (session) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/sessions/${session.id}`,
      }).then(() => {
        session = undefined;
      });
    }
  });

  it('Sessions menu should load Sessions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('session');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Session').should('exist');
    cy.url().should('match', sessionPageUrlPattern);
  });

  describe('Session page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(sessionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Session page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/session/new$'));
        cy.getEntityCreateUpdateHeading('Session');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/sessions',
          body: sessionSample,
        }).then(({ body }) => {
          session = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/sessions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [session],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(sessionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Session page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('session');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPageUrlPattern);
      });

      it('edit button click should load edit Session page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Session');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPageUrlPattern);
      });

      it('edit button click should load edit Session page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Session');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPageUrlPattern);
      });

      it('last delete button click should delete instance of Session', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('session').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', sessionPageUrlPattern);

        session = undefined;
      });
    });
  });

  describe('new Session page', () => {
    beforeEach(() => {
      cy.visit(`${sessionPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Session');
    });

    it('should create an instance of Session', () => {
      cy.get(`[data-cy="startDate"]`).type('2023-11-16T01:46');
      cy.get(`[data-cy="startDate"]`).blur();
      cy.get(`[data-cy="startDate"]`).should('have.value', '2023-11-16T01:46');

      cy.get(`[data-cy="endDate"]`).type('2023-11-16T15:42');
      cy.get(`[data-cy="endDate"]`).blur();
      cy.get(`[data-cy="endDate"]`).should('have.value', '2023-11-16T15:42');

      cy.get(`[data-cy="qrCode"]`).type('maim modulo wordy');
      cy.get(`[data-cy="qrCode"]`).should('have.value', 'maim modulo wordy');

      cy.get(`[data-cy="sessionStatus"]`).select('Undone');

      cy.get(`[data-cy="isNotified"]`).should('not.be.checked');
      cy.get(`[data-cy="isNotified"]`).click();
      cy.get(`[data-cy="isNotified"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(201);
        session = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(200);
      });
      cy.url().should('match', sessionPageUrlPattern);
    });
  });
});
