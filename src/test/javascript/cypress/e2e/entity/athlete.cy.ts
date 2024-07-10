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

describe('Athlete e2e test', () => {
  const athletePageUrl = '/athlete';
  const athletePageUrlPattern = new RegExp('/athlete(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const athleteSample = { name: 'alert huzzah toward' };

  let athlete;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/athletes+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/athletes').as('postEntityRequest');
    cy.intercept('DELETE', '/api/athletes/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (athlete) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/athletes/${athlete.id}`,
      }).then(() => {
        athlete = undefined;
      });
    }
  });

  it('Athletes menu should load Athletes page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('athlete');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Athlete').should('exist');
    cy.url().should('match', athletePageUrlPattern);
  });

  describe('Athlete page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(athletePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Athlete page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/athlete/new$'));
        cy.getEntityCreateUpdateHeading('Athlete');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', athletePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/athletes',
          body: athleteSample,
        }).then(({ body }) => {
          athlete = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/athletes+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [athlete],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(athletePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Athlete page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('athlete');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', athletePageUrlPattern);
      });

      it('edit button click should load edit Athlete page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Athlete');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', athletePageUrlPattern);
      });

      it('edit button click should load edit Athlete page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Athlete');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', athletePageUrlPattern);
      });

      it('last delete button click should delete instance of Athlete', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('athlete').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', athletePageUrlPattern);

        athlete = undefined;
      });
    });
  });

  describe('new Athlete page', () => {
    beforeEach(() => {
      cy.visit(`${athletePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Athlete');
    });

    it('should create an instance of Athlete', () => {
      cy.get(`[data-cy="name"]`).type('queasily brandish from');
      cy.get(`[data-cy="name"]`).should('have.value', 'queasily brandish from');

      cy.get(`[data-cy="email"]`).type('Bekaz8hl_Kutlay@hotmail.com');
      cy.get(`[data-cy="email"]`).should('have.value', 'Bekaz8hl_Kutlay@hotmail.com');

      cy.get(`[data-cy="phone"]`).type('+90-778-594-94-25');
      cy.get(`[data-cy="phone"]`).should('have.value', '+90-778-594-94-25');

      cy.get(`[data-cy="city"]`).type('Yalova');
      cy.get(`[data-cy="city"]`).should('have.value', 'Yalova');

      cy.get(`[data-cy="address"]`).type('hm inhere kindly');
      cy.get(`[data-cy="address"]`).should('have.value', 'hm inhere kindly');

      cy.get(`[data-cy="birthday"]`).type('2023-11-16T15:41');
      cy.get(`[data-cy="birthday"]`).blur();
      cy.get(`[data-cy="birthday"]`).should('have.value', '2023-11-16T15:41');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(201);
        athlete = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(200);
      });
      cy.url().should('match', athletePageUrlPattern);
    });
  });
});
